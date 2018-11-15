package hv.dev4u.org.administradorfirebase;

import android.graphics.Bitmap;

import java.io.Serializable;

public class Producto implements Serializable {
    public String nombre,precio,id_producto,ruta_imagen;
    public Bitmap imagenProducto;


    public Producto(){
        nombre=precio=id_producto="";
        imagenProducto = null;
    }


    public Producto(String id_producto, String nombre, String precio) {
        this.nombre = nombre;
        this.precio = precio;
        this.id_producto = id_producto;
    }

    public Producto(String nombre, String precio, String id_producto, String ruta_imagen, Bitmap imagenProducto) {
        this.nombre = nombre;
        this.precio = precio;
        this.id_producto = id_producto;
        this.ruta_imagen = ruta_imagen;
        this.imagenProducto = imagenProducto;
    }

    public String getRuta_imagen() {
        return ruta_imagen;
    }

    public void setRuta_imagen(String ruta_imagen) {
        this.ruta_imagen = ruta_imagen;
    }

    public Bitmap getImagenProducto() {
        return imagenProducto;
    }

    public void setImagenProducto(Bitmap imagenProducto) {
        this.imagenProducto = imagenProducto;
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
