package socket.client.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomResponse {
    private int responseCode;
    private String responseStatus;

    public CustomResponse(String responseStatus) {
        this.responseStatus=responseStatus;
    }
}
