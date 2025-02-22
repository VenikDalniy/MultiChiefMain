package ru.goryachev.multichief.orchestras.engineeringfunctionorchestrator.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import ru.goryachev.multichief.orchestras.engineeringfunctionorchestrator.app.WebAppInit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration testing with real DB.
 * Necessary to use Lifecycle.PER_CLASS (@TestInstance) to work with common id (testId variable).
 * An entity with autogenerated ID suppose to be created as LinkedHashMap in the first method (1).
 * The ID will be used in each method (2,4,5).
 * The entity with ID suppose to be deleted in the last one (5) and check if getting by the ID throws an Exception.
 * @author Lev Goryachev
 * @version 1.0
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = WebAppInit.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProxyBimServiceIntegrationTest {

    private Long testId;

    private final Logger log = LoggerFactory.getLogger(ProxyBimServiceIntegrationTest.class);

    @Autowired
    private ProxyBimService proxyBimService;

    @Test
    @Order(1)
    @Rollback(value = false)
    public void saveBimTest(){
        Map<String, Object> newBim = new LinkedHashMap<>();
        newBim.put("projectCodeNumber","TestValueAbc");
        newBim.put("projectName","Test Value DEF");
        newBim.put("lod","500");
        newBim.put("projectTypeId","");
        newBim.put("eirId","");
        newBim.put("link","TestValueXyz");
        Object microserviceResponse = proxyBimService.save(newBim);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<Object, Object> responseMap = objectMapper.convertValue(microserviceResponse, Map.class);
        this.testId = Long.parseLong(responseMap.get("id").toString());

        if (log.isDebugEnabled()) {
            log.debug("ProxyBimService" + ", " + "save():" + " " + responseMap.toString());
        }

        Assertions.assertThat(this.testId).isGreaterThan(0);
    }

    @Test
    @Order(2)
    public void getBimByIdTest(){
        Object microserviceResponse = proxyBimService.getOne(testId);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<Object, Object> dtoMap = objectMapper.convertValue(microserviceResponse, Map.class);

        Assertions.assertThat(Long.parseLong(dtoMap.get("id").toString())).isEqualTo(testId);
    }

    @Test
    @Order(3)
    public void getAllBimTest(){
        List<Object> microserviceResponse = proxyBimService.getAll();
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<Object, Object>> dtoMaps = microserviceResponse.stream().map(n ->(Map<Object, Object>) objectMapper.convertValue(n, Map.class)).collect(Collectors.toList());

        Assertions.assertThat(dtoMaps.size()).isGreaterThan(0);
    }

    @Test
    @Order(4)
    @Rollback(value = false)
    public void updateBimTest(){
        ObjectMapper objectMapper = new ObjectMapper();

        Object oldDto = proxyBimService.getOne(testId);
        Map<String, Object> oldDtoMap = objectMapper.convertValue(oldDto, Map.class);
        oldDtoMap.put("projectName","Updated test Value");

        Object dtoUpdated = proxyBimService.save(oldDtoMap);

        if (log.isDebugEnabled()) {
            log.debug("ProxyBimService" + ", " + "update():" + " " + dtoUpdated.toString());
        }

        Object microserviceResponse = proxyBimService.getOne(testId);
        Map<Object, Object> dtoUpdatedReaponse = objectMapper.convertValue(microserviceResponse, Map.class);

        Assertions.assertThat(dtoUpdatedReaponse.get("projectName")).isEqualTo("Updated test Value");
    }
    @Test
    @Order(5)
    @Rollback(value = false)
    public void deleteBimTest(){

        Object microserviceResponse = proxyBimService.delete(testId);
        if (log.isDebugEnabled()) {
            log.debug("ProxyBimService" + ", " + "delete():" + " " + microserviceResponse);
        }
        assertThrows(Exception.class, ()->{proxyBimService.getOne(testId);} );
    }
}