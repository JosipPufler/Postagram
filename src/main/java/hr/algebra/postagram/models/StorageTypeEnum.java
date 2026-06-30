package hr.algebra.postagram.models;

public enum StorageTypeEnum {
    S3,
    DB;

    public static StorageTypeEnum valueOfOrElse(String name) {
        for (StorageTypeEnum value : values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return DB;
    }
}
