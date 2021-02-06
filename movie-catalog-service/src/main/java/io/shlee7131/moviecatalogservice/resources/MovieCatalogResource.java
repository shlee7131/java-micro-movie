package io.shlee7131.moviecatalogservice.resources;

import com.netflix.discovery.converters.Auto;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.shlee7131.moviecatalogservice.models.CatalogItem;
import io.shlee7131.moviecatalogservice.models.Movie;
import io.shlee7131.moviecatalogservice.models.Rating;
import io.shlee7131.moviecatalogservice.models.UserRating;
import io.shlee7131.moviecatalogservice.services.MovieInfo;
import io.shlee7131.moviecatalogservice.services.UserRatingInfo;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {
    // Application.java 에 있는 Bean으로부터 정보를 받아온다(Fetch) -> RestTemplate 인스턴스 생성
    // REST Call 을 위한 객체 (RestTemplate) 일종의 핸들러
    @Autowired
    private RestTemplate restTemplate;


    // RestTemplate와 다르게 비동기 처리를 한다
    // builder 인스턴스를 만들어 처리해야 하는 call 마다 객체를 만들어서 비동기 처리를 하게 만든다(1 call 1 builder)
    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    MovieInfo movieInfo;

    @Autowired
    UserRatingInfo userRatingInfo;

    @RequestMapping("/{userId}") //PathVariable을 통해 userId 값 매핑
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId) {

        // Eureka Server에 등록된 이름을 주소 대신에 활용(service discovery)
        UserRating userRating = userRatingInfo.getUserRating(userId);

        return userRating.getRatings().stream().map(rating ->
                    // For each movie ID, call movie info service and get details
                    // 1. RestTemplate 을 활용
                    // getForObject : 목표 API의 URL 정보를 통한 REST Call -> 해당 API로부터 String 반환 -> jSON 형태의 String을 Movie 클래스에 맞게 파싱(Unmarshal)


                    // 2. WebClient를  활용
//                    Movie movie = webClientBuilder.build()
//                            .get()
//                            .uri("http://localhost:8082/movies/" + rating.getMovieId())
//                            .retrieve()
//                            .bodyToMono(Movie.class)
//                            .block();
                    // bodyToMono -> 받아온 정보를 Movie 클래스로 파싱, Mono는 비동기 처리에서 목표 객체로의 정보를 담는다.(불완전한 정보)
                    // block() 을 통해 Mono가 목표 객체의 정보를 다 담을 때까지 기다린다. -> 이후에 block/non-block으로 이어진다.
                    // 완전한 비동기 + non-block I/O 를 위해선 block을 없애고 반환되는 값을 Mono타입으로 받아줘야 한다.

                    // Put them all together
                    //new CatalogItem("Transformers", "Test", 4)
                movieInfo.getCatalogItem(rating))
                .collect(Collectors.toList());

    }





    public List<CatalogItem> getFallbackCatalog(@PathVariable("userId") String userId) {
        return Arrays.asList(new CatalogItem("No movie", "", 0));
    }
        // dummy data
//        return Collections.singletonList(
//                new CatalogItem("Transformers", "Test", 4)
//        );
}

