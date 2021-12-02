import com.google.gson.Gson;
import com.reltio.cst.dataload.domain.CreditsBalance;
import com.reltio.cst.dataload.domain.StatusResponse;

public class CreditsSerializationTest {

    public static void main(String[] args) throws Exception {
        Gson gson = new Gson();
        CreditsBalance balance = gson.fromJson("{\"primary\":{\"priorityCredits\":100.00,\"standardSyncCredits\":59.50,\"standardAsyncCredits\":100.00},\"matching\":{\"priorityCredits\":100.00,\"standardSyncCredits\":100.00,\"standardAsyncCredits\":100.00},\"timestamp\":1637792087242,\"tenantId\":\"KishorPushparaj\",\"customerName\":\"CustKishor\"}", CreditsBalance.class);

        if (balance.getPrimaryBalance().getStandardSyncCredits() != 59.50) {
            throw new Exception("invalid credits parse result");
        }
        if (balance.getPrimaryBalance().getPriorityCredits() != 100) {
            throw new Exception("invalid credits parse result");
        }
        if (balance.getPrimaryBalance().getStandardAsyncCredits() != 100) {
            throw new Exception("invalid credits parse result");
        }
    }
}
