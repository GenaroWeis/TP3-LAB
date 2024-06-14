package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.TipoCuenta;
import ar.edu.utn.frbb.tup.model.TipoMoneda;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaNoSoportadaException;
import ar.edu.utn.frbb.tup.persistence.CuentaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class CuentaService {
    CuentaDao cuentaDao = new CuentaDao();

    @Autowired
    ClienteService clienteService;

    //Generar casos de test para darDeAltaCuenta
    //    1 - cuenta existente
    //    2 - cuenta no soportada
    //    3 - cliente ya tiene cuenta de ese tipo
    //    4 - cuenta creada exitosamente

    private static final Set<String> TIPOS_CUENTA_SOPORTADA = new HashSet<>();

    static {
        TIPOS_CUENTA_SOPORTADA.add(TipoCuenta.CAJA_AHORRO + "-" + TipoMoneda.PESOS);
        TIPOS_CUENTA_SOPORTADA.add(TipoCuenta.CUENTA_CORRIENTE + "-" + TipoMoneda.PESOS);
        TIPOS_CUENTA_SOPORTADA.add(TipoCuenta.CAJA_AHORRO + "-" + TipoMoneda.DOLARES);
        TIPOS_CUENTA_SOPORTADA.add(TipoCuenta.CUENTA_CORRIENTE + "-" + TipoMoneda.DOLARES);
    }

    public boolean tipoDeCuentaSoportada(Cuenta cuenta) {
        String tipoCuenta = cuenta.getTipoCuenta() + "-" + cuenta.getMoneda();
        return TIPOS_CUENTA_SOPORTADA.contains(tipoCuenta);
    }
    
    public void darDeAltaCuenta(Cuenta cuenta, long dniTitular) throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException, TipoCuentaNoSoportadaException {
       //CUENTA EXISTENTE
        if(cuentaDao.find(cuenta.getNumeroCuenta()) != null) {
            throw new CuentaAlreadyExistsException("La cuenta " + cuenta.getNumeroCuenta() + " ya existe.");
        }

        //CUENTA NO SOPORTADA. Chequear cuentas soportadas por el banco CA$ CC$ CAU$S
        if (!tipoDeCuentaSoportada(cuenta)) {
            throw new TipoCuentaNoSoportadaException("El tipo de cuenta " + cuenta.getTipoCuenta() + " en " + cuenta.getMoneda() + " no está soportado.");
        }
        
        //CUENTA CREADA EXITOSAMENTE
        clienteService.agregarCuenta(cuenta, dniTitular);
        cuentaDao.save(cuenta);
    }

    public Cuenta find(long id) {
        return cuentaDao.find(id);
    }
}
