package io.reactiverse.awssdk.integration;

import cloud.localstack.Localstack;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import static io.reactiverse.awssdk.VertxSdkClient.withVertx;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class LocalStackBaseSpec {

    protected static final AwsCredentialsProvider credentialsProvider = () -> new AwsCredentials() {
        @Override
        public String accessKeyId() {
            return "a";
        }

        @Override
        public String secretAccessKey() {
            return "a";
        }
    };

    protected void assertContext(Vertx vertx, Context context, VertxTestContext testContext) {
        testContext.verify(() -> {
            assertEquals(context, vertx.getOrCreateContext());
        });
    }

    protected static URI s3URI() throws Exception {
        return new URI(Localstack.INSTANCE.getEndpointS3());
    }

    protected static S3AsyncClient s3(Context context) throws Exception {
        final S3AsyncClientBuilder builder = S3AsyncClient.builder()
                .serviceConfiguration(sc ->
                        sc.checksumValidationEnabled(false)
                                .pathStyleAccessEnabled(true) // from localstack documentation
                )
                .credentialsProvider(credentialsProvider)
                .endpointOverride(s3URI())
                .region(Region.EU_WEST_1);
        return withVertx(builder, context).build();
    }


    protected static <T> Single<T> single(CompletableFuture<T> future) {
        final SingleOnSubscribe<T> sos = emitter ->
                future.handle((result, error) -> {
                    if (error != null) {
                        emitter.onError(error);
                    } else {
                        emitter.onSuccess(result);
                    }
                    return future;
                });
        return Single.create(sos);
    }

}
