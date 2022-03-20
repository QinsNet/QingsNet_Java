package cure.tradition;


import cure.tradition.dto.FeelPulseRequestDTO;
import cure.tradition.dto.FeelPulseResponseDTO;
import cure.tradition.service.FeelPulseService;

public class main {

    public static void main(String[] args) {
FeelPulseService service = new FeelPulseService();
FeelPulseRequestDTO requestDTO = new FeelPulseRequestDTO();
FeelPulseResponseDTO responseDTO = service.feel(requestDTO);
if(responseDTO.isSuccess()){
    System.out.println(responseDTO.getResult());
}
else System.out.println("切诊失败");
    }

}
