package com.reltio.cst.dataload.impl.helper;

import au.com.bytecode.opencsv.CSVReader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.reltio.cst.dataload.domain.DataloaderInput;
import com.reltio.cst.dataload.domain.ReltioDataloadErrors;
import com.reltio.cst.service.ReltioAPIService;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProcessTrackerServiceTest {

    @Test
    public void multiExceptions() throws Exception {
        DataloaderInput dataloaderInput = mock(DataloaderInput.class);
        Type errorType = new TypeToken<Map<Integer, List<ReltioDataloadErrors>>>() {
        }.getType();
        Gson gson = new Gson();
        String input = new String(Files.readAllBytes(Paths.get(getClass().getResource("/dvf/error.json").getPath())));
        Map<Integer, List<ReltioDataloadErrors>> errors = gson.fromJson(input, errorType);
        when(dataloaderInput.getFailedRecordsFileName()).thenReturn("input.json");
        when(dataloaderInput.getDataloadErrorsMap()).thenReturn(errors);
        when(dataloaderInput.getFailedRecordsFileName()).thenReturn("error_output");
        ReltioAPIService reltioAPIService = mock(ReltioAPIService.class);
        ProcessTrackerService service = new ProcessTrackerService(dataloaderInput, reltioAPIService);
        service.sendProcessTrackerUpdate(true);

        try (FileReader filereader = new FileReader("error_output_failurelog.csv");
             CSVReader reader = new CSVReader(filereader)) {
            List<String[]> rows = reader.readAll();
            for (String row[] : rows) {
                String crosswalkValue = row[1];
                switch (crosswalkValue) {
                    case "45555555555555": {
                        Assert.assertEquals("Type is not found for URI configuration/entityTypes/Individual and tenant testtenant", row[4]);
                        break;
                    }
                    case "effe30f2-6ae0-4ba6-a8ec-d51f97ec57b3": {
                        Assert.assertEquals("First Name is required one ;LastName is required one message ", row[4]);
                        break;
                    }
                    case "d3a326f3-b4fd-4ff6-aa27-a1dc2bc61e67": {
                        Assert.assertEquals("First Name is required one ;Age is required one message ", row[4]);
                        break;
                    }
                }
            }
        }
    }
}
