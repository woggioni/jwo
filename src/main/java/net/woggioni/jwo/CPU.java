package net.woggioni.jwo;

public enum CPU {
    X86("x86"),
    AMD64("amd64"),
    ARM("arm"),
    AARCH64("aarch64"),
    S390("s390"),
    S390X("s390x"),
    RISCV("riscv"),
    RISCV64("riscv64");


    private final String value;

    CPU(String value) {
        this.value = value;
    }

    public static CPU current;

    static {
        String archName = System.getProperty("os.arch").toLowerCase();
        for(CPU cpu : values()) {
            if(archName.startsWith(cpu.value)) {
                current = cpu;
            }
        }
        if(current == null) throw new IllegalArgumentException(String.format("Unrecognized cpu arch '%s'", archName));
    }
}