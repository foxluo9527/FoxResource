import android.media.MediaCodecList
import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.ExecuteCallback
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFprobe
import com.arthenica.mobileffmpeg.Level
import com.foxluo.baselib.util.TimeUtil.nowTime
import java.io.File

object VideoCompress {
    private var totalDuration: Double = 0.0
    private var lastProgressTime = 0L

    fun compressVideo(
        inputPath: String?,
        outputPath: String?,
        listener: CompressionListener?
    ): Long {
        // 增强路径校验
        if (inputPath.isNullOrEmpty() || outputPath.isNullOrEmpty()) {
            listener?.onFailure("文件路径不能为空")
            return -1
        }

        val inputFile = File(inputPath)
        val outputFile = File(outputPath)

        // 增强文件校验
        if (!inputFile.exists() || !inputFile.canRead()) {
            listener?.onFailure("输入文件不可读或不存在")
            return -1
        }

        return try {
            // 强制创建输出目录
            outputFile.parentFile?.let {
                if (!it.exists() && !it.mkdirs()) {
                    listener?.onFailure("无法创建输出目录")
                    return -1
                }
            }
            totalDuration = getTotalDuration(inputPath)
            Config.enableLogCallback { log ->
                Log.e("FFmpegLog", "[${log.level}] ${log.text}")
                if (log.level == Level.AV_LOG_INFO) {
                    parseLogForProgress(log.text, listener)
                }
            }

            val configInfo = Config.isLTSBuild()
            Log.d("FFmpegLog", "isLTSBuild:${configInfo.toString()}")

            val command = buildCompressionCommand(inputPath, outputPath)

            listener?.onStart()

            FFmpeg.executeAsync(command, object : ExecuteCallback {
                override fun apply(executionId: Long, returnCode: Int) {
                    when {
                        returnCode == Config.RETURN_CODE_SUCCESS -> {
                            when {
                                !outputFile.exists() -> listener?.onFailure("输出文件未生成")
                                outputFile.length() < 1024 -> listener?.onFailure("输出文件过小")
                                else -> listener?.onSuccess()
                            }
                        }

                        returnCode == Config.RETURN_CODE_CANCEL -> listener?.onCancel()
                        else -> {
                            val error = when (returnCode) {
                                1 -> "参数错误或编解码器问题"
                                137 -> "内存不足"
                                else -> "未知错误 (${Config.getLastCommandOutput()})"
                            }
                            listener?.onFailure("压缩失败: $error")
                        }
                    }
                    Config.enableLogCallback(null) // 关闭日志
                }
            })
        } catch (e: Exception) {
            listener?.onFailure("初始化异常: ${e.localizedMessage}")
            -1
        }
    }
    private fun parseLogForProgress(log: String, listener: CompressionListener?) {
        try {
            // 匹配时间信息（格式：00:00:28.46）
            val timeRegex = Regex("""time=(\d+:\d+:\d+\.\d+)""")
            timeRegex.find(log)?.let { match ->
                val timeStr = match.groupValues[1]
                val currentSec = parseTimeToSeconds(timeStr)

                // 计算进度百分比
                if (totalDuration > 0) {
                    val progress = (currentSec / totalDuration * 100).coerceIn(0.0, 100.0)

                    // 限流：每秒最多更新4次
                    if (nowTime - lastProgressTime > 250) {
                        lastProgressTime = nowTime
                        listener?.onProgress(progress.toFloat())
                    }
                }
            }

            // 匹配帧数信息（可选）
            val frameRegex = Regex("""frame=\s*(\d+)""")
            frameRegex.find(log)?.let { match ->
                val frame = match.groupValues[1].toInt()
                // 如果需要使用帧数计算进度可以在此处理
            }
        } catch (e: Exception) {
            Log.w("Progress", "进度解析失败: ${e.message}")
        }
    }

    private fun parseTimeToSeconds(timeStr: String): Double {
        val parts = timeStr.split(":")
        return when (parts.size) {
            3 -> { // HH:MM:SS.ss
                parts[0].toDouble() * 3600 +
                        parts[1].toDouble() * 60 +
                        parts[2].toDouble()
            }
            2 -> { // MM:SS.ss
                parts[0].toDouble() * 60 +
                        parts[1].toDouble()
            }
            else -> parts[0].toDouble() // SS.ss
        }
    }

    private fun getTotalDuration(inputPath: String?): Double {
        return try {
            FFprobe.getMediaInformation(inputPath)?.duration?.toDouble() ?: 0.0
        } catch (e: Exception) {
            Log.e("Duration", "获取时长失败: ${e.message}")
            0.0
        }
    }

    fun buildCompressionCommand(inputPath: String, outputPath: String): Array<String> {
        return arrayOf(
            "-y",
            "-i", inputPath,
            "-c:v", "libx264",
            "-preset", "ultrafast",      // 最快的预设
            "-tune", "fastdecode",       // 优化解码速度
            "-x264-params", "threads=4:no-scenecut=1", // 固定线程数
            "-pix_fmt", "yuv420p",
            "-crf", "26",                // 稍高的CRF值
            "-r", "30",                  // 限制帧率
            "-c:a", "aac",
            "-b:a", "96k",
            outputPath
        )
    }

    fun checkHardwareEncoderSupport(): Boolean {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        for (codecInfo in codecList.codecInfos) {
            if (codecInfo.isEncoder) {
                for (type in codecInfo.supportedTypes) {
                    if (type.equals("video/avc", ignoreCase = true)) {
                        Log.d("MediaCodec", "Supported H.264 encoder: ${codecInfo.name}")
                        return true
                    }
                }
            }
        }
        return false
    }

    fun cancel(executionId: Long) {
        FFmpeg.cancel(executionId)
    }

    interface CompressionListener {
        fun onStart()
        fun onSuccess()
        fun onProgress(percent: Float) // 进度百分比 (0-100)
        fun onFailure(error: String)
        fun onCancel()
    }
}