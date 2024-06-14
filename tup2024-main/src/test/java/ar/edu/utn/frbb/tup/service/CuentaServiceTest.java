package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.*;
import ar.edu.utn.frbb.tup.model.exception.ClienteAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaNoSoportadaException;
import ar.edu.utn.frbb.tup.persistence.ClienteDao;
import ar.edu.utn.frbb.tup.persistence.CuentaDao;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CuentaServiceTest {
    
    //CREAMOS LOS MOCKS
    //imitan el comportamiento de los objetos reales, pero permiten controlar y verificar sus interacciones sin necesidad de depender de la implementación completa de estos objetos.
     @Mock
    private CuentaDao cuentaDao;

    @Mock
    private ClienteDao clienteDao;

    @Mock
    private ClienteService clienteService;

    @Mock
    private Cliente cliente;

    //INJECTAMOS LOS MOCKS
    //le decimos que cuentaService utilizará los mocks en lugar de las implementaciones reales
    @InjectMocks
    private CuentaService cuentaService;

    //inicializa los mocks anotados con @Mock y @InjectMocks
    @BeforeAll
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCuentaYaExiste() throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException, TipoCuentaNoSoportadaException {

        Cuenta CuentaYaExiste = new Cuenta();//se instancia el objeto Cuenta
        CuentaYaExiste.setNumeroCuenta(1133);//con el numero de cuenta 1133
        long dnititular = 123456789;

        when(cuentaDao.find(1133)).thenReturn(CuentaYaExiste);// Se configura cuentaDao para que devuelva la cuenta creada cuando se le pase el número 1133. Esto simula la existencia previa de esa cuenta en el sistema (mock)


        boolean exceptionThrown = false;
        //Verificación de que al intentar dar de alta la cuenta, se lanza la excepción CuentaAlreadyExistsException
            try {
                cuentaService.darDeAltaCuenta(CuentaYaExiste, dnititular);
            } catch (CuentaAlreadyExistsException e) {
                exceptionThrown = true;
            }

        assertTrue(exceptionThrown, "error revisar, se tendria que lanzar la excepcion");//para chequear
}


    @Test
    public void testTipoCuentaNoSoportada()
            throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException, TipoCuentaNoSoportadaException {
        Cuenta cuentaNoSoportada = new Cuenta();//se instancia el objeto Cuenta que no va a ser soportada
        cuentaNoSoportada.setTipoCuenta(null);//pongo null para simular que no se soporta este tipo de cuenta

        doReturn(null).when(cuentaDao).find(anyLong());//Se configura el mock de cuentaDao para que el método find siempre retorne null con cualquier long que se ingrese. Esto simula que la cuenta no existe en la base de datos.

        boolean exceptionThrown = false;
        // Intentamos dar de alta la cuenta y atrapamos la excepción esperada
        try {
            cuentaService.darDeAltaCuenta(cuentaNoSoportada, 123456789);
        } catch (TipoCuentaNoSoportadaException e) {
            exceptionThrown = true;
   
            assertTrue(exceptionThrown, "error revisar, se tendria que lanzar la excepcion");//para chequear
    }
    }

    @Test
    public void testClienteYaTieneUnaCuentaDeEseTipo() throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException, TipoCuentaNoSoportadaException {
        Cuenta cuenta = new Cuenta();
        cuenta.setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE);
        cuenta.setMoneda(TipoMoneda.PESOS);

        Cliente pepeRino = new Cliente();
        pepeRino.setDni(12345678);
        pepeRino.addCuenta(cuenta);
        pepeRino.setNombre("pepe");
        pepeRino.setApellido("Rino");
        pepeRino.setFechaNacimiento(LocalDate.of(2005, 6,7));
        pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);

        when(cuentaDao.find(cuenta.getNumeroCuenta())).thenReturn(null);//Se configura el mock de cuentaDao para que el método find siempre retorne null con cualquier long que se ingrese. Esto simula que la cuenta no existe en la base de datos.
        doThrow(TipoCuentaAlreadyExistsException.class).when(clienteService).agregarCuenta(cuenta, pepeRino.getDni());//Se configura el mock de clienteDao para que el método save siempre lance una excepción de que ya existe la cuenta cuando Cliente que se ingrese. Esto simula que el cliente ya tiene una cuenta de ese tipo.
        
        boolean exceptionThrown = false;
        try {
            // Intentar dar de alta la cuenta
            cuentaService.darDeAltaCuenta(cuenta, pepeRino.getDni());
        } catch (TipoCuentaAlreadyExistsException e) {
            //Capturar la excepción
            exceptionThrown = true;
        }
    
        assertTrue(exceptionThrown, "error revisar, se tendria que lanzar la excepcion");//para chequear
    }

  
@Test
public void testCuentaCreadaExitosamente() throws TipoCuentaNoSoportadaException, TipoCuentaAlreadyExistsException, CuentaAlreadyExistsException {
    Cuenta cuenta = new Cuenta();
    cuenta.setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE);
    cuenta.setMoneda(TipoMoneda.PESOS);

    Cliente pepeRino = new Cliente();
    pepeRino.setDni(12345678);
    pepeRino.setNombre("pepe");
    pepeRino.setApellido("Rino");

    // Configurar el mock para que el método find devuelva null (simulando que la cuenta no existe), 
    when(cuentaDao.find(anyLong())).thenReturn(null);

    // Configurar el mock del clienteService para que no haga nada cuando se llame al método agregarCuenta() con cualquier objeto Cuenta y cualquier número DNI, Esto evita que se ejecute la implementación real del método, ya que solo nos interesa verificar que se llame
    doNothing().when(clienteService).agregarCuenta(any(Cuenta.class), anyLong());

    // Llamar al método darDeAltaCuenta pasándole la cuenta creada y el DNI. Esto simula que el cliente ya tenga una cuenta de ese tipo y que se llame al método save() del mock de cuentaDao para que se guarde la cuenta en la base de datos.
    cuentaService.darDeAltaCuenta(cuenta, pepeRino.getDni());

    // Verificar que se llamen los métodos esperados, Esto asegura que el servicio de cliente fue invocado correctamente durante el proceso de creación de la cuenta
    verify(clienteService, times(1)).agregarCuenta(cuenta, pepeRino.getDni());
    //verifica que el método save() del cuentaDao se haya llamado una vez con la cuenta como argumento. Esto asegura que la cuenta se guardó correctamente en la base de datos.
    verify(cuentaDao, times(1)).save(cuenta);
}
}
