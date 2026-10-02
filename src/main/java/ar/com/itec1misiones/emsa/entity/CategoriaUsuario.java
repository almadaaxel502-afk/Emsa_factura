package ar.com.itec1misiones.emsa.entity;
public enum CategoriaUsuario {
    PRIMEROS_30_KWH(30), SIGUIENTE_90_KWH(90), SIGUIENTE_54_KWH(54);
    private final int kwhBloque;
    CategoriaUsuario(int kwhBloque) { this.kwhBloque = kwhBloque; }
    public int getKwhBloque() { return kwhBloque; }
}
