/*
 * Copyright 2018 Akashic Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.zhihexireng.core;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import dev.zhihexireng.core.store.TransactionPool;
import dev.zhihexireng.core.store.datasource.DbSource;
import dev.zhihexireng.proto.BlockChainProto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class TransactionManager {
    private final DbSource db;
    private final TransactionPool txPool;
    private final Set<byte[]> unconfirmedTxSet = new HashSet<>();

    @Autowired
    public TransactionManager(DbSource db, TransactionPool transactionPool) {
        this.db = db;
        this.db.init();
        this.txPool = transactionPool;
    }

    public Transaction put(byte[] key, Transaction tx) {
        try {
            txPool.put(key, tx);
            unconfirmedTxSet.add(key);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tx;
    }

    public Transaction get(byte[] key) {
        Transaction foundTx = txPool.get(key);
        return foundTx != null ? foundTx : deserialize(db.get(key));
    }

    public void batchAll() {
        this.batch(unconfirmedTxSet);
    }

    public void batch(Set<byte[]> keys) {
        if(keys.size() > 0) {
            Map<byte[], Transaction> map = txPool.getAll(keys);
            System.out.println(map);
            for (byte[] key : map.keySet()) {
                db.put(key, serialize(map.get(key)));
            }
            this.flush();
        }
    }

    public Collection<Transaction> getUnconfirmedTxs () {
        Map<byte[], Transaction> unconfirmedTxs = txPool.getAll(unconfirmedTxSet);
        return unconfirmedTxs.values();
    }

    public int count() {
        return unconfirmedTxSet.size();
    }

    public void flush() {
        txPool.remove(unconfirmedTxSet);
        unconfirmedTxSet.clear();
    }

    private byte[] serialize(Transaction transaction) {
        return convertToProto(transaction).toByteArray();
    }

    private Transaction deserialize(byte[] stream) {
        return convertToObject(stream);
    }

    private BlockChainProto.Transaction convertToProto(Transaction tx) {
        TransactionHeader txHeader = tx.getHeader();
        BlockChainProto.TransactionHeader header =
                BlockChainProto.TransactionHeader.newBuilder()
                        .setType(ByteString.copyFrom(txHeader.getType()))
                        .setVersion(ByteString.copyFrom(txHeader.getVersion()))
                        .setDataHash(ByteString.copyFrom(txHeader.getDataHash()))
                        .setTimestamp(txHeader.getTimestamp())
                        .setDataSize(txHeader.getDataSize())
                        .setSignature(ByteString.copyFrom(txHeader.getSignature()))
                        .build();
        return BlockChainProto.Transaction.newBuilder()
                .setHeader(header)
                .setData(tx.getData())
                .build();
    }

    private Transaction convertToObject(byte[] stream) {
        BlockChainProto.Transaction txProto = null;
        try {
            txProto = BlockChainProto.Transaction.parseFrom(stream);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        if(txProto == null) {
            return null;
        }

        BlockChainProto.TransactionHeader txHeaderProto = txProto.getHeader();
        TransactionHeader txHeader = new TransactionHeader(
                txHeaderProto.getType().toByteArray(),
                txHeaderProto.getVersion().toByteArray(),
                txHeaderProto.getDataHash().toByteArray(),
                txHeaderProto.getTimestamp(),
                txHeaderProto.getDataSize(),
                txHeaderProto.getSignature().toByteArray()
        );

        return new Transaction(txHeader, txProto.getData());
    }
}
