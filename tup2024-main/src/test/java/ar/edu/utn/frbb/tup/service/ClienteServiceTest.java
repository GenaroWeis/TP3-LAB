package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.*;
import ar.edu.utn.frbb.tup.model.exception.ClienteAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.persistence.ClienteDao;
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
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClienteServiceTest {

    @Mock
    private ClienteDao clienteDao;//indica que clientedao es un mock

    @InjectMocks
    private ClienteService clienteService;//

    @BeforeAll
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testClienteMenor18Años() {
        Cliente clienteMenorDeEdad = new Cliente();
        clienteMenorDeEdad.setFechaNacimiento(LocalDate.of(2020, 2, 7));
        assertThrows(IllegalArgumentException.class, () -> clienteService.darDeAltaCliente(clienteMenorDeEdad));
    }

    @Test
    public void testClienteSuccess() throws ClienteAlreadyExistsException {
        Cliente cliente = new Cliente();
        cliente.setFechaNacimiento(LocalDate.of(1978,3,25));
        cliente.setDni(29857643);
        cliente.setTipoPersona(TipoPersona.PERSONA_FISICA);
        clienteService.darDeAltaCliente(cliente);

        verify(clienteDao, times(1)).save(cliente);
    }

    @Test
    public void testClienteAlreadyExistsException() throws ClienteAlreadyExistsException {
        Cliente pepeRino = new Cliente();
        pepeRino.setDni(26456437);
        pepeRino.setNombre("Pepe");
        pepeRino.setApellido("Rino");
        pepeRino.setFechaNacimiento(LocalDate.of(1978, 3,25));
        pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);

        when(clienteDao.find(26456437, false)).thenReturn(new Cliente());

        assertThrows(ClienteAlreadyExistsException.class, () -> clienteService.darDeAltaCliente(pepeRino));
    }



    @Test
    public void testAgregarCuentaAClienteSuccess() throws TipoCuentaAlreadyExistsException {
        Cliente pepeRino = new Cliente();
        pepeRino.setDni(26456439);
        pepeRino.setNombre("Pepe");
        pepeRino.setApellido("Rino");
        pepeRino.setFechaNacimiento(LocalDate.of(1978, 3,25));
        pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);

        Cuenta cuenta = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        when(clienteDao.find(26456439, true)).thenReturn(pepeRino);

        clienteService.agregarCuenta(cuenta, pepeRino.getDni());

        verify(clienteDao, times(1)).save(pepeRino);

        assertEquals(1, pepeRino.getCuentas().size());
        assertEquals(pepeRino, cuenta.getTitular());

    }


    @Test
    public void testAgregarCuentaAClienteDuplicada() throws TipoCuentaAlreadyExistsException {
        Cliente luciano = new Cliente();
        luciano.setDni(26456439);
        luciano.setNombre("Pepe");
        luciano.setApellido("Rino");
        luciano.setFechaNacimiento(LocalDate.of(1978, 3,25));
        luciano.setTipoPersona(TipoPersona.PERSONA_FISICA);

        Cuenta cuenta = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        when(clienteDao.find(26456439, true)).thenReturn(luciano);

        clienteService.agregarCuenta(cuenta, luciano.getDni());

        Cuenta cuenta2 = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        assertThrows(TipoCuentaAlreadyExistsException.class, () -> clienteService.agregarCuenta(cuenta2, luciano.getDni()));
        verify(clienteDao, times(1)).save(luciano);
        assertEquals(1, luciano.getCuentas().size());
        assertEquals(luciano, cuenta.getTitular());

    }

    //Agregar una CA$ y CC$ --> success 2 cuentas, titular peperino
    @Test
    public void testAgregarCuentaCA_CC_piola() throws TipoCuentaAlreadyExistsException { //en caso de ser necesario tira la exception

        Cliente pepeRino = new Cliente();//creo un cliente
        pepeRino.setDni(12345678);
        pepeRino.setNombre("pepe");
        pepeRino.setApellido("Rino");
        pepeRino.setFechaNacimiento(LocalDate.of(2005, 6,7));
        pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);

        Cuenta cuentaAhorro = new Cuenta()//creo una caja de ahorro (cuenta)
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(100000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        Cuenta cuentaCorriente = new Cuenta()//creo una cuenta corriente (cuenta)
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(100000)
                .setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE);

        when(clienteDao.find(12345678, true)).thenReturn(pepeRino);

        clienteService.agregarCuenta(cuentaAhorro, pepeRino.getDni());
        clienteService.agregarCuenta(cuentaCorriente, pepeRino.getDni());

        verify(clienteDao, times(2)).save(pepeRino);
        assertEquals(2, pepeRino.getCuentas().size());
        assertTrue(pepeRino.getCuentas().contains(cuentaAhorro));
        assertTrue(pepeRino.getCuentas().contains(cuentaCorriente));
        assertEquals(pepeRino, cuentaAhorro.getTitular());
        assertEquals(pepeRino, cuentaCorriente.getTitular());
    }

        //Agregar una CA$ y CAU$ --> success 2 cuentas, titular peperino
        @Test
        public void testAgregarCuentaCA_CCUSD() throws TipoCuentaAlreadyExistsException {//en caso de ser necesario tira la exception
    
            Cliente pepeRino = new Cliente();//creo un cliente
            pepeRino.setDni(12345678);
            pepeRino.setNombre("pepe");
            pepeRino.setApellido("Rino");
            pepeRino.setFechaNacimiento(LocalDate.of(2005, 6,7));
            pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);
    
            Cuenta cuentaAhorro = new Cuenta()//creo una caja de ahorro (cuenta)
                    .setMoneda(TipoMoneda.PESOS)
                    .setBalance(100000)
                    .setTipoCuenta(TipoCuenta.CAJA_AHORRO);
    
            Cuenta cuentaAhorroUSD = new Cuenta()//creo una caja de ahorro USD (cuenta)
                    .setMoneda(TipoMoneda.DOLARES)
                    .setBalance(100000)
                    .setTipoCuenta(TipoCuenta.CAJA_AHORRO);
    
            when(clienteDao.find(12345678, true)).thenReturn(pepeRino);//como se indico que clientedao es un mock se usa el find de la biblioteca de mocks
    
            clienteService.agregarCuenta(cuentaAhorro, pepeRino.getDni());
            clienteService.agregarCuenta(cuentaAhorroUSD, pepeRino.getDni());
    
            verify(clienteDao, times(2)).save(pepeRino);
            
            assertEquals(2, pepeRino.getCuentas().size());//Se verifica que el cliente pepeRino tiene exactamente dos cuentas
            assertTrue(pepeRino.getCuentas().contains(cuentaAhorro));//Se verifica que cuentaAhorro está en la lista de cuentas de pepeRino
            assertTrue(pepeRino.getCuentas().contains(cuentaAhorroUSD));//Se verifica que cuentaAhorroUSD está en la lista de cuentas de pepeRino
            assertEquals(pepeRino, cuentaAhorro.getTitular());//Se verifica que el titular de cuentaAhorro es pepeRino
            assertEquals(pepeRino, cuentaAhorroUSD.getTitular());//Se verifica que el titular de cuentaAhorroUSD es pepeRino
        }

        //Testear clienteService.buscarPorDni

    @Test
    public void testBuscarPorDni() throws Exception{
        Cliente pepeRino = new Cliente();
        pepeRino.setDni(12345678);
        pepeRino.setNombre("pepe");
        pepeRino.setApellido("Rino");
        pepeRino.setFechaNacimiento(LocalDate.of(2005, 6,7));
        pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);

        when(clienteDao.find(12345678,true)).thenReturn(pepeRino);//como se indico que clientedao es un mock se usa el find de la biblioteca de mocks

        Cliente clienteEncontrado = clienteService.buscarClientePorDni(12345678);//se busca al cliente de igual dni

        assertEquals(pepeRino, clienteEncontrado);//si el cliente ingresado (mock) es igual al cliente que se buscó, se pasa el test
    }
    //Agregar una CA$ y CC$ --> success 2 cuentas, titular peperino
    //Agregar una CA$ y CAU$S --> success 2 cuentas, titular peperino...
    //Testear clienteService.buscarPorDni
}