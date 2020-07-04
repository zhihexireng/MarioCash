/*
 * Copyright 2018 Akashic Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.zhihexireng.node.config;

import dev.zhihexireng.contract.CoinContract;
import dev.zhihexireng.contract.Contract;
import dev.zhihexireng.core.BlockChain;
import dev.zhihexireng.core.BlockChainLoader;
import dev.zhihexireng.core.BlockHusk;
import dev.zhihexireng.core.store.BlockStore;
import dev.zhihexireng.core.store.TransactionStore;
import dev.zhihexireng.core.store.datasource.DbSource;
import dev.zhihexireng.core.store.datasource.LevelDbDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class YeedBranchConfiguration {

    @Value("classpath:/branch-yeed.json")
    Resource resource;

    @Bean(name = "yeedBranch")
    BlockChain blockChain(@Qualifier("yeedGenesis")BlockHusk genesisBlock, BlockStore blockStore,
                          TransactionStore transactionStore,
                          @Qualifier("yeedContract") Contract contract) {
        return new BlockChain(genesisBlock, blockStore, transactionStore, contract);
    }

    @Bean(name = "yeedGenesis")
    BlockHusk genesisBlock() throws IOException {
        return new BlockChainLoader(resource.getInputStream()).getGenesis();
    }

    @Bean(name = "yeedContract")
    Contract yeedContract() {
        return new CoinContract();
    }


    @Profile("prod")
    @Primary
    @Bean(name = "blockDbSource")
    DbSource blockLevelDbSource(@Qualifier("yeedGenesis")BlockHusk genesisBlock) {
        return new LevelDbDataSource(genesisBlock.getHash() + "/blocks");
    }

    @Profile("prod")
    @Primary
    @Bean(name = "txDbSource")
    DbSource txLevelDbSource(@Qualifier("yeedGenesis")BlockHusk genesisBlock) {
        return new LevelDbDataSource(genesisBlock.getHash() + "/txs");
    }

}