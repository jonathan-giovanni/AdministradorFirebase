# AdministradorFirebase
Administrador de datos utilizando cloud firestore



<img src="https://github.com/jonathancplusplus/AdministradorFirebase/blob/master/capturas/app_admin.jpg" >

<b>Este proyecto tiene las siguientes caracteristicas: </b>

* Permite: Crear, Modificar, Eliminar y Consultar documentos pertenecientes a la colección "productos"
* Lista de datos se actualiza automaticamente
* Implementación eficente dado que solo se actualizan los elementos modificados, NO toda la lista 
* Uso de Cloud Firestore en lugar de Realtime database por su eficiencia y facilidad
* En caso de que no se posea conexión a internet carga los ultimos datos descargados
* Permite almacenar imagenes en Firestorage dentro de una carpeta llamada "imagenes_productos"

<b>Estructura de la base de datos</b>

El tipo de BD que maneja firebase es no relacional (NoSQL) por lo que trabaja con coleciones y documentos, en este caso la colección utilizada en la app es <b>productos</b> y se puede observar que contiene una serie de documentos con id's generados automaticamente, y que ademas cada documentos posee una estructura siguiente:

La estructura de la BD es la siguiente

     
      ->productos              es la raiz
           -> 40YEcz..         es el id generado por defecto del documento
               -> nombre       nombre del producto
               -> precio       precio del producto
               -> ruta_imagen  ruta a la imagen almacenada en firestorage
           -> Rjya4i5..        es el id generado por defecto del documento
               -> nombre       nombre del producto
               -> precio       precio del producto
               -> ruta_imagen  ruta a la imagen almacenada en firestorage
          .
          .
          .
          Hasta N documentos bajo la coleccion productos
          

<img src="https://github.com/jonathancplusplus/AdministradorFirebase/blob/master/capturas/cloud_firestore.png" >


La forma de guardar la imagen es dentro de un folder llamado <b>imagenes_productos</b>

<img src="https://github.com/jonathancplusplus/AdministradorFirebase/blob/master/capturas/storage.png" >
