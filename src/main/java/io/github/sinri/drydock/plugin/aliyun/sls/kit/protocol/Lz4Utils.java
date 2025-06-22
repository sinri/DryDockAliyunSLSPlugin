package io.github.sinri.drydock.plugin.aliyun.sls.kit.protocol;

import io.vertx.core.buffer.Buffer;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

/**
 * LZ4压缩工具类
 */
public class Lz4Utils {
    private static final LZ4Factory factory = LZ4Factory.fastestInstance();
    private static final LZ4Compressor compressor = factory.fastCompressor();
    
    /**
     * 使用LZ4算法压缩Buffer
     * 
     * @param buffer 待压缩的Buffer
     * @return 压缩后的Buffer
     */
    public static Buffer compress(Buffer buffer) {
        if (buffer == null || buffer.length() == 0) {
            return Buffer.buffer();
        }
        
        byte[] srcBytes = buffer.getBytes();
        int maxCompressedLength = compressor.maxCompressedLength(srcBytes.length);
        byte[] compressedBytes = new byte[maxCompressedLength];
        
        int compressedLength = compressor.compress(srcBytes, 0, srcBytes.length, compressedBytes, 0);
        
        // 创建只包含实际压缩数据的Buffer
        byte[] resultBytes = new byte[compressedLength];
        System.arraycopy(compressedBytes, 0, resultBytes, 0, compressedLength);
        return Buffer.buffer(resultBytes);
    }
}
