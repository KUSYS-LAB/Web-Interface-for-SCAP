package ac.kr.korea.cdm.dto;

public class CdmErrorDto extends RuntimeException {
    public CdmErrorDto(String msg) {super(msg);}
}
