package com.restful.payload.auth.album;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AlbumDTO  {

    @NotBlank
    @Schema(description = "Album name", example = "Bikaner", requiredMode = RequiredMode.REQUIRED)
    private String name;
    
     @NotBlank
    @Schema(description = "Album Description", example = "Forts", requiredMode = RequiredMode.REQUIRED)
    private String description;
}
