package ac.kr.korea.cdm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MemberDto {
    private String id;
    private String password;
    private String firstNameEn;
    private String lastNameEn;
    private String firstNameKo;
    private String lastNameKo;
    private String countryCode;
    private String institute;
    public int typeCode;


    public boolean isFilled() {
        this.sanitize();
        if (this.id != null
                && this.password != null
                && this.firstNameEn != null
                && this.lastNameEn != null
                && this.firstNameKo != null
                && this.lastNameKo != null
                && this.countryCode != null
                && (this.typeCode == 1 || this.typeCode == 2)) return true;
        return false;
    }

    public void sanitize() {
        if (this.id != null && this.id.equals("")) this.id = null;
        if (this.password != null && this.password.equals("")) this.password = null;
        if (this.firstNameEn != null && this.firstNameEn.equals("")) this.firstNameEn = null;
        if (this.lastNameEn != null && this.lastNameEn.equals("")) this.lastNameEn = null;
        if (this.firstNameKo != null && this.firstNameKo.equals("")) this.firstNameKo = null;
        if (this.lastNameKo != null && this.lastNameKo.equals("")) this.lastNameKo = null;
        if (this.countryCode != null && this.countryCode.equals("")) this.countryCode = null;
        if (this.institute != null && this.institute.equals("")) this.institute = null;
        if (!(this.typeCode == 1 || this.typeCode == 2)) this.typeCode = 0;
    }

}
