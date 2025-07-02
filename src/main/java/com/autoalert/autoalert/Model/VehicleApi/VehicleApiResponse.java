package com.autoalert.autoalert.Model.VehicleApi;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VehicleApiResponse {
    private int total_count;
    private List<VehicleRecord> results;

}
