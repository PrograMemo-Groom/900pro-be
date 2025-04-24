package programo._pro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@SpringBootApplication
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // Cross-Origin Resource Sharing (CORS)
    // http://http://localhost:5173/ 으로 부터 오는 모든 요청 허용하기
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**") // 모든 경로에 대한 HTTP 요청을 처리
                        .allowedMethods("*") // 모든 http 메소드를 허용
                        .allowedOrigins("http://localhost:5173", "http://localhost:5174") // "http://localhost:5173"에서 오는 요청만 허용
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }


}
