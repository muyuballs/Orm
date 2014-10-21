package info.breezes.orm;

import info.breezes.orm.annotation.Column;
import info.breezes.orm.annotation.Table;

import java.io.Serializable;

@Table(name = "Contacts")
public class Employee implements Serializable {

    private static final long serialVersionUID = -5576314467281405644L;

    @Column(primaryKey = true, uniqueIndex = true, autoincrement = true)
    public int employee_id;
    @Column
    public int company_id;
    @Column
    public int department_id;
    @Column
    public int position_id;
    @Column
    public String employee_name;
    public String pinyin;
    @Column
    public int employee_sex;
    @Column
    public String employee_birthdate;
    @Column
    public String employee_nationality;
    @Column
    public String employee_code;
    @Column
    public String employee_polit;
    @Column
    public String employee_martial_status;
    @Column
    public String employee_avatar;
    @Column
    public String employee_hiredate;
    @Column
    public String employee_firedate;
    @Column
    public String employee_cellphone;
    @Column
    public String employee_email;
    @Column
    public String employee_qq;
    @Column
    public String employee_phone_number;
    @Column
    public String employee_probation;
    @Column
    public String insurance_tpl;
    @Column
    public String insurance_company;
    @Column
    public String salary_tpl;
    @Column
    public String attendance_tpl;
    @Column
    public String employee_password;
    @Column
    public String employee_idcard_type;
    @Column
    public String employee_idcard_code;
    @Column
    public String employee_idcard_office;
    @Column
    public String employee_idcard_expire;
    @Column
    public String employee_idcard_address;
    @Column
    public String employee_birthplace;
    @Column
    public String employee_address;
    @Column
    public String employee_current_status;
    @Column
    public String employee_level;
    @Column
    public String contract_effecitve_date;
    @Column
    public String contract_expiry_date;
    @Column
    public String contract_expiry_times;
    @Column
    public String employee_probation_start;
    @Column
    public String employee_probation_end;
    @Column
    public String dep_name;
    @Column
    public String pos_name;
    @Column
    public String education_id;
    @Column
    public String education_type;
    @Column
    public String education_subject;
    @Column
    public String education_record;
    @Column
    public String education_dirving_licence;
    @Column
    public String employee_title;
    @Column
    public String education_language_type;
    @Column
    public String education_language_level;
    @Column
    public String salary_tpl_value;
    @Column
    public String salary_tpl_from;
    @Column
    public String insurance_tpl_value;
    @Column
    public String insurance_tpl_from;
    @Column
    public String attendance_tpl_value;
    @Column
    public String attendance_tpl_from;

}
