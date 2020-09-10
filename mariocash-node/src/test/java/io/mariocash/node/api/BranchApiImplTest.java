package dev.zhihexireng.node.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.zhihexireng.core.Runtime;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.Wallet;
import dev.zhihexireng.core.store.StateStore;
import dev.zhihexireng.core.store.TransactionReceiptStore;
import dev.zhihexireng.node.TestUtils;
import dev.zhihexireng.node.controller.TransactionDto;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class BranchApiImplTest {
    private static final Logger log = LoggerFactory.getLogger(BranchApiImplTest.class);
    private static final BranchApi branchApi = new JsonRpcConfig().branchApi();
    private static final Runtime runtime =
            new Runtime(new StateStore(), new TransactionReceiptStore());
    private static final Map<String, JsonObject> branchStoreMock = new HashMap<>();
    private static Wallet wallet;
    private static String branchId;

    @Before
    public void setUp() throws Exception {
        this.wallet = new Wallet();
    }

    @Test
    public void branchApiIsNotNull() {
        assertThat(branchApi).isNotNull();
    }

    @Test
    public void create() {
        try {
            JsonObject branch = TestUtils.getSampleBranch1();
            branchId = TestUtils.getBranchId(branch);
            JsonArray params = new JsonArray();
            JsonObject param = new JsonObject();
            param.addProperty("branchId", branchId);
            param.add("branch", branch);
            params.add(param);

            JsonObject txObj = new JsonObject();
            txObj.addProperty("method", "create");
            txObj.add("params", params);

            TransactionHusk tx = TestUtils.createTxHuskByJson(txObj).sign(wallet);
            branchApi.createBranch(TransactionDto.createBy(tx));
        } catch (Exception e) {
             e.printStackTrace();
        }
    }

    @Test
    public void update() {
    }

    @Test
    public void search() {
    }

    @Test
    public void view() throws Exception {
        try {
            create();
            JsonArray params = new JsonArray();
            JsonObject param = new JsonObject();
            param.addProperty("branchId", branchId);
            params.add(param);

            JsonObject queryObj = TestUtils.createQuery("view", params);
            branchApi.viewBranch(queryObj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getCurrentVersion() {
        try {
            create();
            JsonArray params = new JsonArray();
            JsonObject param = new JsonObject();
            param.addProperty("branchId", branchId);
            params.add(param);

            JsonObject queryObj = TestUtils.createQuery("getcurrentversion", params);
            branchApi.getCurrentVersionOfBranch(queryObj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getVersionHistory() {
        try {
            create();
            JsonArray params = new JsonArray();
            JsonObject param = new JsonObject();
            param.addProperty("branchId", branchId);
            params.add(param);

            JsonObject queryObj = TestUtils.createQuery("getversionhistory", params);
            branchApi.getVersionHistoryOfBranch(queryObj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}