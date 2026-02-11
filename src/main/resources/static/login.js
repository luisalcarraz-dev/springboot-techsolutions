function validarLogin() {

    const usuario = document.getElementById("usuario").value.trim();
    const contrasena = document.getElementById("contrasena").value.trim();
    const mensajeError = document.getElementById("mensaje-error");

    if (usuario === "" || contrasena === "") {
        mensajeError.textContent = "Todos los campos son obligatorios";
        return false;
    }

    if (contrasena.length < 4) {
        mensajeError.textContent = "La contraseña debe tener al menos 4 caracteres";
        return false;
    }

    mensajeError.textContent = "";
    return true;
}
