package com.autoalert.autoalert.Service;

import com.autoalert.autoalert.Model.VehicleApi.VehicleApiResponse;
import com.autoalert.autoalert.Model.VehicleApi.VehicleRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleApiService {

    private final RestTemplate restTemplate;

    private final String API_URL = "https://public.opendatasoft.com/api/explore/v2.1/catalog/datasets/all-vehicles-model/records?limit=100";

    public List<String> getAllMakes() {
        VehicleApiResponse response = restTemplate.getForObject(API_URL, VehicleApiResponse.class);

        if (response != null && response.getResults() != null) {
            return response.getResults().stream()
                    .map(VehicleRecord::getMake)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public Map<String, List<String>> getModelsGroupedByMake() {
        VehicleApiResponse response = restTemplate.getForObject(API_URL, VehicleApiResponse.class);

        if (response != null && response.getResults() != null) {
            return response.getResults().stream()
                    .filter(v -> v.getMake() != null && v.getModel() != null)
                    .collect(Collectors.groupingBy(
                            VehicleRecord::getMake,
                            Collectors.mapping(VehicleRecord::getModel, Collectors.toList())
                    ));
        }
        return Map.of();
    }

    public boolean isValidCar(String make, String model) {
        Map<String, List<String>> all = getModelsGroupedByBrand();
        return all.containsKey(make) && all.get(make).contains(model);
    }

    public Map<String, List<String>> getModelsGroupedByBrand() {
        String jsonResponse = restTemplate.getForObject(API_URL, String.class);
        Map<String, Set<String>> grouped = new HashMap<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            JsonNode results = root.get("results");

            if (results != null && results.isArray()) {
                for (JsonNode record : results) {
                    String make = record.path("make").asText();
                    String model = record.path("model").asText();

                    if (!make.isBlank() && !model.isBlank()) {
                        grouped.computeIfAbsent(make, k -> new TreeSet<>()).add(model);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return grouped.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())));
    }

}
