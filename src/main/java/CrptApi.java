import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
public class CrptApi {

    private long period;
    private int requestLimit;
    private Integer counter = 0;
    private ObjectMapper objectMapper;


    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.period = timeUnit.toMillis(1);
        this.requestLimit = requestLimit;
    }


    public void createRFProduct(JsonDoc jsonDoc, String signature) throws JsonProcessingException {

        synchronized (counter) {
            while (counter == requestLimit) {
                try {
                    Thread.sleep(period / 10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            new Thread(() -> {
                try {
                    counter++;
                    Thread.sleep(period);
                    counter--;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        HttpRequest.newBuilder()
                .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(jsonDoc)))
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString(signature.getBytes()))
                .build();
    }

    @Data
    class JsonDoc {
        ParticipantDescription description;
        String doc_id;
        String doc_status;
        DocType doc_type;
        String importRequest;
        String owner_inn;
        String participant_inn;
        String producer_inn;
        LocalDate production_date;
        String production_type;
        List<Product> products;
        LocalDate reg_date;
        String reg_number;
    }

    @Data
    class ParticipantDescription {
        String description;
    }

    enum DocType {
        LP_INTRODUCE_GOODS
    }

    @Data
    class Product {
        String certificate_document;
        LocalDate certificate_document_date;
        String certificate_document_number;
        String owner_inn;
        String producer_inn;
        LocalDate production_date;
        String tnved_code;
        String uit_code;
        String uitu_code;
    }
}

