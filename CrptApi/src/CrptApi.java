import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


    public class CrptApi {
        static String url = "https://ismp.crpt.ru/api/v3/lk/documents/create";
        // JSON-документ
        static String jsonInputString = "{\"description\": { \"participantInn\": \"string\" }, \"doc_id\": \"string\", \"doc_status\": \"string\", \"doc_type\": \"LP_INTRODUCE_GOODS\", 109 \"importRequest\": true, \"owner_inn\": \"string\", \"participant_inn\": \"string\", \"producer_inn\": \"string\", \"production_date\": \"2020-01-23\", \"production_type\": \"string\", \"products\": [ { \"certificate_document\": \"string\", \"certificate_document_date\": \"2020-01-23\", \"certificate_document_number\": \"string\", \"owner_inn\": \"string\", \"producer_inn\": \"string\", \"production_date\": \"2020-01-23\", \"tnved_code\": \"string\", \"uit_code\": \"string\", \"uitu_code\": \"string\" } ], \"reg_date\": \"2020-01-23\", \"reg_number\": \"string\"}";
        // Строка подписи
        static String signature = "Ваша строка подписи";
        static Object document = "Документ";

        public static void main(String[] args) {
            CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 10);
            for (int i = 0; i < 10; i++) {
                Thread thread = new Thread(() -> {
                    //  crptApi.createDocument(new Object(), "signature");
                    crptApi.createDocument(document, signature);
                });
                thread.start();
            }
        }
        private final TimeUnit timeUnit; //указывает промежуток времени – секунда, минута и пр.
        private final int requestLimit; // положительное значение, которое определяет максимальное количество запросов в этом промежутке времени.
        private final AtomicInteger requestCount;
        private final Object lock;

        public CrptApi(TimeUnit timeUnit, int requestLimit) {
            this.timeUnit = timeUnit;
            this.requestLimit = requestLimit;
            this.requestCount = new AtomicInteger(0);
            this.lock = new Object();
        }

        public void createDocument(Object document, String signature) {
            synchronized (lock) {
                long startTime = System.currentTimeMillis();
                long endTime = startTime + timeUnit.toMillis(1);

                int currentRequestCount = requestCount.incrementAndGet();
                while (currentRequestCount > requestLimit) {
                    try {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime >= endTime) {
                            requestCount.set(1);
                            break;
                        }
                        long remainingTime = endTime - currentTime;
                        lock.wait(remainingTime);
                        currentRequestCount = requestCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                // Make API call and handle the response //Сделать вызов API и обработать ответ
                if (currentRequestCount <= requestLimit) {
                    try {
                        System.out.println("API call successful"); //Вызов API успешен

                        // Установка соединения
                        URL obj = new URL(url);
                        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

                        // Установка метода запроса
                        connection.setRequestMethod("POST");

                        // Установка заголовков
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setRequestProperty("Signature", signature);

                        // Разрешение вывода в тело запроса
                        connection.setDoOutput(true);

                        // Отправка JSON-документа
                        try (OutputStream os = connection.getOutputStream()) {
                            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                            os.write(input, 0, input.length);
                        }

                        // Получение ответа
                        int responseCode = connection.getResponseCode();
                        System.out.println("Ответ сервера: " + responseCode);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("API call blocked due to request limit exceeded"); //Вызов API заблокирован из-за превышения лимита запросов
                }
            }
        }

}

