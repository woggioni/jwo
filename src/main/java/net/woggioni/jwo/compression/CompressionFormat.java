package net.woggioni.jwo.compression;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CompressionFormat {
    XZ("xz", "xz"),
    BZIP2("bzip2", "bz2"),
    GZIP("gzip", "gz"),
    LZMA("lzma", "lzma"),
    LZO("lzop", "lzo"),
    LZ4("lz4", "lz4"),
    ZSTD("zstd", "zst");

    public final String executable;
    public final String suffix;
}
