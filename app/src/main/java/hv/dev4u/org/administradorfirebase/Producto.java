package hv.dev4u.org.administradorfirebase;

import java.io.Serializable;

public class Producto implements Serializable {
    public String nombre,precio,id_producto;
    public Producto(){
        nombre=precio=id_producto="";
    }
    public Producto(String id_producto, String nombre, String precio) {
        this.nombre = nombre;
        this.precio = precio;
        this.id_producto = id_producto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public String getId_producto() {
        return id_producto;
    }

    public void setId_producto(String id_producto) {
        this.id_producto = id_producto;
    }
}
