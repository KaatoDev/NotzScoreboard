package notzSb.model.entities.enums;

public enum Staff {
    AJUDANTE(1),
    TRIAL(2),
    MODERADOR(3),
    ADMIN(4),
    GERENTE(5),
    DIRETOR(6),
    PLAYER(7);

    private final int id;

    Staff(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
