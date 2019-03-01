package dev.zhihexireng.core;

import dev.zhihexireng.crypto.HashUtil;
import dev.zhihexireng.util.ByteUtil;
import dev.zhihexireng.util.SerializeUtils;
import dev.zhihexireng.util.TimeUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * The type Block header.
 */
public class BlockHeader implements Serializable {

    private final byte[] type;
    private final byte[] version;
    private final byte[] prevBlockHash;
    private final byte[] merkleRoot;
    private final long timestamp;
    private final long dataSize;
    private final byte[] signature;

    private final long index;

    private BlockHeader(Builder builder) {
        this.type = builder.type;
        this.version = builder.version;
        this.prevBlockHash = builder.prevBlockHash;
        this.merkleRoot = builder.merkleRoot;
        this.timestamp = builder.timestamp;
        this.dataSize = builder.dataSize;
        this.signature = builder.signature;

        this.index = builder.index;
    }

    /**
     * Gets index.
     *
     * @return the index
     */
    public long getIndex() {
        return index;
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getType() {
        return type;
    }

    public byte[] getVersion() {
        return version;
    }

    public byte[] getMerkleRoot() {
        return merkleRoot;
    }

    public long getDataSize() {
        return dataSize;
    }

    public byte[] getSignature() {
        return signature;
    }

    /**
     * Get block hash byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getBlockHash() throws IOException {
        ByteArrayOutputStream block = new ByteArrayOutputStream();

        block.write(type);
        block.write(version);
        if (prevBlockHash != null) {
            block.write(prevBlockHash);
        }
        if (merkleRoot != null) {
            block.write(merkleRoot);
        }
        block.write(ByteUtil.longToBytes(timestamp));
        block.write(ByteUtil.longToBytes(dataSize));
        block.write(signature);
        block.write(ByteUtil.longToBytes(index));

        return HashUtil.sha3(block.toByteArray());
    }

    /**
     * Get prev block hash byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getPrevBlockHash() {
        return prevBlockHash;
    }

    /**
     * The type Builder.
     */
    public static class Builder {

        private final byte[] type;
        private final byte[] version;
        private byte[] prevBlockHash;
        private byte[] merkleRoot;
        private long timestamp;
        private long dataSize;
        private byte[] signature;

        private long index;

        /**
         * Instantiates a new Builder.
         */
        public Builder() {
            type = new byte[4];
            version = new byte[4];
        }

        /**
         * Prev block builder.
         *
         * @param prevBlock the prev block
         * @return the builder
         */
        public Builder prevBlock(Block prevBlock) throws IOException {
            if (prevBlock == null) {
                this.index = 0;
                this.prevBlockHash = null;
            } else {
                this.index = prevBlock.nextIndex();
                this.prevBlockHash = prevBlock.getBlockByteHash();
            }
            return this;
        }

        /**
         * Block body builder.
         *
         * @param blockBody the block body
         * @return the builder
         */
        public Builder blockBody(BlockBody blockBody) throws IOException {
            this.merkleRoot = blockBody.getMerkleRoot();
            this.dataSize = blockBody.getSize();
            return this;
        }

        /**
         * Build block header.
         *
         * @return the block header
         */
        @Deprecated
        public BlockHeader build(Account from) throws IOException {
            timestamp = TimeUtils.getCurrenttime();
            this.signature = from.getKey().sign(this.getDataHashForSigning()).toBinary();
            return new BlockHeader(this);
        }

        /**
         * Build block header with wallet.
         *
         * @return the block header
         */
        public BlockHeader build(Wallet wallet) throws IOException {
            timestamp = TimeUtils.getCurrenttime();
            this.signature = wallet.signHashedData(this.getDataHashForSigning());
            return new BlockHeader(this);
        }

        /**
         * Build block header.
         *
         * @return the block header
         */
        public BlockHeader build(long index, byte[] prev, long timestamp, byte[] signature) {
            this.index = index;
            this.prevBlockHash = prev;
            this.timestamp = timestamp;
            this.signature = signature;

            return new BlockHeader(this);
        }

        /**
         * Get data hash for signing.
         *
         * @return hash of sign data
         * @throws IOException IOException
         */
        private byte[] getDataHashForSigning() throws IOException {

            if (type == null) {
                throw new IOException("getDataHashForSigning(): type is null");
            }

            if (version == null) {
                throw new IOException("getDataHashForSigning(): version is null");
            }

            ByteArrayOutputStream block = new ByteArrayOutputStream();

            block.write(type);
            block.write(version);

            if (prevBlockHash == null) {
                prevBlockHash = new byte[32];
            }
            block.write(prevBlockHash);

            if (merkleRoot == null) {
                merkleRoot = new byte[32];
            }
            block.write(merkleRoot);

            block.write(ByteUtil.longToBytes(timestamp));
            block.write(ByteUtil.longToBytes(dataSize));

            return HashUtil.sha3(block.toByteArray());
        }
    }
}
