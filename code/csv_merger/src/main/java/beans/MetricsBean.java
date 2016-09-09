package beans;

public class MetricsBean {
    String Project_Name, ClassName, CBO, DIT, ILCOM, LOC, LOD_Class, NOM;

    Integer ID;
    String Segment;
    String S;       //S_N_Days

    public void setProject_Name(String project_Name) {
        this.Project_Name = project_Name;
    }

    public String getProject_Name() {
        return Project_Name;
    }

    public void setClassName(String className) {
        this.ClassName = className;
    }

    public String getClassName() {
        return ClassName;
    }

    public void setCBO(String cbo) {
        this.CBO = cbo;
    }

    public String getCBO() {
        return CBO;
    }

    public void setDIT(String dit) {
        this.DIT = dit;
    }

    public String getDIT() {
        return DIT;
    }

    public void setILCOM(String ilcom) {
        ILCOM = ilcom;
    }

    public String getILCOM() {
        return ILCOM;
    }

    public void setLOC(String loc) {
        LOC = loc;
    }

    public String getLOC() {
        return LOC;
    }

    public void setLOD_Class(String lod_class) {
        LOD_Class = lod_class;
    }

    public String getLOD_Class() {
        return LOD_Class;
    }

    public void setNOM(String nom) {
        NOM = nom;
    }

    public String getNOM() {
        return NOM;
    }

    public void setID(Integer id) {
        ID = id;
    }

    public Integer getID() {
        return ID;
    }

    public void setSegment(String segment) {
        this.Segment = segment;
    }

    public String getSegment() {
        return Segment;
    }

    public void setS(String s) {
        this.S = s;
    }

    public String getS() {
        return S;
    }
}
