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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import dev.zhihexireng.core.exception.FailedOperationException;
import dev.zhihexireng.proto.Proto;
import dev.zhihexireng.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static dev.zhihexireng.core.BranchInfo.BranchData;

public class BlockChainLoader {
    private ObjectMapper mapper = new ObjectMapper();
    private InputStream branchInfoStream;

    public BlockChainLoader(InputStream branchInfoStream) {
        this.branchInfoStream = branchInfoStream;
    }

    public BlockChainLoader(File infoFile) {
        try {
            this.branchInfoStream = new FileInputStream(infoFile);
        } catch (FileNotFoundException e) {
            throw new FailedOperationException(e);
        }
    }

    public BlockHusk getGenesis() throws IOException {
        BranchInfo branchInfo = loadBranchInfo();
        //TODO 브랜치 정보 파일 컨버팅
        return convertBlock(branchInfo);
    }

    public BranchInfo loadBranchInfo() throws IOException {
        return mapper.readValue(branchInfoStream, BranchInfo.class);
    }

    private BlockHusk convertBlock(BranchInfo branchInfo) {
        ByteString prevBlockHash = ByteString.copyFrom(Hex.decode(branchInfo.prevBlockHash
                .getBytes()));
        Proto.Block.Builder builder = Proto.Block.newBuilder()
                .setHeader(Proto.Block.Header.newBuilder()
                        .setRawData(Proto.Block.Header.Raw.newBuilder()
                                .setType(ByteString.copyFrom(Hex.decode(branchInfo.type)))
                                .setVersion(ByteString.copyFrom(Hex.decode(branchInfo.version)))
                                .setIndex(0)
                                .setTimestamp(ByteUtil.byteArrayToLong(
                                        Hex.decode(branchInfo.timestamp)))
                                .setPrevBlockHash(prevBlockHash)
                                .setMerkleRoot(ByteString.copyFrom(branchInfo.merkleRoot
                                        .getBytes()))
                                .setDataSize(ByteUtil.byteArrayToLong(
                                        Hex.decode(branchInfo.dataSize)))
                                .build()
                        )
                        .setSignature(ByteString.copyFrom(Hex.decode(branchInfo.signature)))
                        .build());
        if (branchInfo.data != null && !branchInfo.data.isEmpty()) {
            builder.addAllBody(convertTransaction(branchInfo.data));
        }
        return new BlockHusk(builder.build());
    }

    private List<Proto.Transaction> convertTransaction(List<BranchData> branchDataList) {
        List<Proto.Transaction> list = new ArrayList<>();
        for (BranchData branchData : branchDataList) {
            ByteString byteString = ByteString.copyFrom(Hex.decode(branchData.dataHash));
            list.add(Proto.Transaction.newBuilder()
                    .setHeader(Proto.Transaction.Header.newBuilder()
                            .setRawData(Proto.Transaction.Header.Raw.newBuilder()
                                    .setType(ByteString.copyFrom(Hex.decode(branchData.type)))
                                    .setVersion(ByteString.copyFrom(Hex.decode(branchData.version)))
                                    .setTimestamp(ByteUtil.byteArrayToLong(
                                            Hex.decode(branchData.timestamp)))
                                    .setDataHash(byteString)
                                    .setDataSize(ByteUtil.byteArrayToLong(
                                            Hex.decode(branchData.dataSize)))
                                    .build()
                            )
                            .setSignature(ByteString.copyFrom(Hex.decode(branchData.signature)))
                            .build()
                    )
                    .setBody(branchData.data).build()
            );
        }
        return list;
    }

}
