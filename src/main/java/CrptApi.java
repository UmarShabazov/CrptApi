import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class CrptApi {

    public static void main(String[] args) {

//      RequestExecutor actionator = new InstantRequestExecutor();
//      RequestExecutor actionator = new LoggingRequestExecutor();

        RequestExecutor actionator = new RateLimitedRequestExecutor();

        for (int i = 0; i <= 200; i++) {

            actionator.execute(() -> System.out.println(new Date()));
        }
    }


    public TimeUnit timeUnit;
    public final int requestLimit;
    public Semaphore semaphore;
    private final long intervalMillis;


    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.intervalMillis = timeUnit.toMillis(1);
        this.semaphore = new Semaphore(requestLimit, true);
    }



    public interface RequestExecutor {

        public abstract void execute(Runnable action);

    }

    public static class InstantRequestExecutor implements RequestExecutor {


        @Override
        public void execute(Runnable action) {

            action.run();

        }
    }

    public static class RateLimitedRequestExecutor implements RequestExecutor {
        Queue<Runnable> queue = new LinkedList<>();

        {
            new Thread(() -> {

                while (true) {
                    try {
                        Thread.sleep(1000);
                        if (!queue.isEmpty()) {
                            queue.poll().run();
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            }).start();
        }

        @Override
        public void execute(Runnable action) {


            queue.add(action);
            System.out.println(queue.size());

        }


        public static class LoggingRequestExecutor implements RequestExecutor {

            @Override
            public void execute(Runnable action) {
                System.out.println("Начинаю выполнение действия.");

                action.run();

                System.out.println("Заканчиваю выполнение действия.");


            }
        }

        public static class Document {
            private String participantInn;
            private String docId;
            private String docStatus;
            private String docType = "LP_INTRODUCE_GOODS";
            private boolean importRequest;
            private String ownerInn;
            private String producerInn;
            private String productionDate;
            private String productionType;
            private String regDate;
            private String regNumber;
            // Список продуктов
            private Product[] products;

            // Внутренний класс для представления продукта
            public static class Product {
                private String certificateDocument;
                private String certificateDocumentDate;
                private String certificateDocumentNumber;
                private String ownerInn;
                private String producerInn;
                private String productionDate;
                private String tnvedCode;
                private String uitCode;
                private String uituCode;

            }


        }
    }
}
