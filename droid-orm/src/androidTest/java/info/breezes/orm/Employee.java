/*
 * Copyright (c) 2014-2015, Qiao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the LICENSE
 */

package info.breezes.orm;

import info.breezes.orm.annotation.Column;
import info.breezes.orm.annotation.Table;

import java.io.Serializable;

@Table(name = "Contacts")
public class Employee implements Serializable {

    private static final long serialVersionUID = -5576314467281405644L;

    @Column(name = "id",primaryKey = true, uniqueIndex = true, autoincrement = true)
    public int employee_id;
    @Column(name = "cid")
    public int company_id;
    @Column(name = "did")
    public int department_id;
    @Column(name = "pid")
    public int position_id;
    @Column(name = "en1")
    public String employee_name;
    public String pinyin;
    @Column(name = "es1")
    public int employee_sex;
    @Column(name = "eb1")
    public String employee_birthdate;
    @Column(name = "en")
    public String employee_nationality;
    @Column(name = "ec")
    public String employee_code;
    @Column(name = "ep")
    public String employee_polit;
    @Column(name = "ems")
    public String employee_martial_status;
    @Column(name = "ea")
    public String employee_avatar;
    @Column(name = "ei1")
    public String employee_hiredate;
    @Column(name = "ef")
    public String employee_firedate;
    @Column(name = "el")
    public String employee_cellphone;
    @Column(name = "ee")
    public String employee_email;
    @Column(name = "eq")
    public String employee_qq;
    @Column(name = "epn")
    public String employee_phone_number;
    @Column(name = "er1")
    public String employee_probation;
    @Column(name = "it")
    public String insurance_tpl;
    @Column(name = "ic")
    public String insurance_company;
    @Column(name = "st")
    public String salary_tpl;
    @Column(name = "at")
    public String attendance_tpl;
    @Column(name = "ess")
    public String employee_password;
    @Column(name = "eit")
    public String employee_idcard_type;
    @Column(name = "eic")
    public String employee_idcard_code;
    @Column(name = "eio")
    public String employee_idcard_office;
    @Column(name = "eie")
    public String employee_idcard_expire;
    @Column(name = "eia")
    public String employee_idcard_address;
    @Column(name = "eb")
    public String employee_birthplace;
    @Column(name = "eadd")
    public String employee_address;
    @Column(name = "ecs")
    public String employee_current_status;
    @Column(name = "ele")
    public String employee_level;
    @Column(name = "ced")
    public String contract_effecitve_date;
    @Column(name = "cea")
    public String contract_expiry_date;
    @Column(name = "cet")
    public String contract_expiry_times;
    @Column(name = "eps")
    public String employee_probation_start;
    @Column(name = "epe")
    public String employee_probation_end;
    @Column(name = "dn")
    public String dep_name;
    @Column(name = "pn")
    public String pos_name;
    @Column(name = "ei")
    public String education_id;
    @Column(name = "et")
    public String education_type;
    @Column(name = "es")
    public String education_subject;
    @Column(name = "er")
    public String education_record;
    @Column(name = "edl")
    public String education_dirving_licence;
    @Column(name = "eti")
    public String employee_title;
    @Column(name = "elt")
    public String education_language_type;
    @Column(name = "ell")
    public String education_language_level;
    @Column(name = "stv")
    public String salary_tpl_value;
    @Column(name = "stf")
    public String salary_tpl_from;
    @Column(name = "itv")
    public String insurance_tpl_value;
    @Column(name = "itf")
    public String insurance_tpl_from;
    @Column(name = "atv")
    public String attendance_tpl_value;
    @Column(name = "atf")
    public String attendance_tpl_from;

}
