import com.reltio.cst.dataload.impl.LoadJsonToTenant;

import java.io.*;
import java.util.UUID;

public class LoadJSONToTenantThrottlingTest {

    public static void main(String[] args) throws Exception {
        generateData();
        String[] arguments = new String[] { "src/test/resources/tst-01-throttling-test-config.properties" };
        LoadJsonToTenant.main(arguments);
    }

    public static void generateData() throws Exception {
        FileInputStream stream = new FileInputStream("src/test/resources/tst-01-dataload-template.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String template = reader.readLine();
        reader.close();

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream("src/test/resources/tst-01-dataload.json")));
        for(int k = 0; k < 1000; k++) {
            String entity = template.replace("INDEX", k + "").replace("CROSSWALK", UUID.randomUUID().toString());
            writer.println(entity);
        }
        writer.close();
    }
}

