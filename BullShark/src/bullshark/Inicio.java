package bullshark;

import java.sql.*;
import java.util.*;

public class Inicio {

    static final String URL = "jdbc:mysql://localhost:3306/bullshark?rewriteBatchedStatements=true";
    static final String USER = "bolsa";
    static final String PASS = "bolsa";
    static final String path = "C:/bullshark/cotizaciones";
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            mostrarMenu(con, null, 1);
            System.out.print("Fin");
        }
    }

    // =========================
    // MENÚ RECURSIVO
    // =========================
    private static void mostrarMenu(Connection con, Integer parentId, int nivel) throws SQLException {

        List<MenuItem> items = cargarMenu(con, parentId);

        if (items.isEmpty()) return;

        while (true) {
            System.out.println("\n===== NIVEL " + nivel + " =====");
            for (int i = 0; i < items.size(); i++) {
                System.out.println((i + 1) + ". " + items.get(i).texto);
            }
            if(nivel==1) {
            	System.out.println("0. Salir");
            }else {
            	System.out.println("0. Volver");
            }

            System.out.print("Elige opción: ");
            int op = sc.nextInt();

            if (op == 0) {            	 
            	return;
            }

            MenuItem seleccionado = items.get(op - 1);

            // ¿Tiene hijos?
            if (tieneHijos(con, seleccionado.id)) {
                mostrarMenu(con, seleccionado.id, nivel + 1);
            } else {
                ejecutarAccion(seleccionado.accion, con);
            }
        }
       
    }

    // =========================
    // CARGAR MENÚ POR NIVEL
    // =========================
    private static List<MenuItem> cargarMenu(Connection con, Integer parentId) throws SQLException {
        List<MenuItem> lista = new ArrayList<>();
        String sql = parentId == null ?
                "SELECT * FROM menu WHERE parent_id IS NULL" :
                "SELECT * FROM menu WHERE parent_id = ?";

        PreparedStatement ps = con.prepareStatement(sql);
        if (parentId != null) ps.setInt(1, parentId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            lista.add(new MenuItem(
                    rs.getInt("id"),
                    rs.getString("texto"),
                    rs.getString("accion")
            ));
        }
        return lista;
    }

    private static boolean tieneHijos(Connection con, int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(*) FROM menu WHERE parent_id=?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        rs.next();
        return rs.getInt(1) > 0;
    }

    // =========================
    // ACCIONES
    // =========================
    private static void ejecutarAccion(String accion, Connection con) throws SQLException {
        if (accion == null) return;

        switch (accion) {
            case "verClientes":
                verClientes(con);
                break;
            case "addCliente":
                addCliente(con);
                break;
            case "verProductos":
                verProductos(con);
                break;
            case "addProducto":
                addProducto(con);
                break;
            case "importarExcel1":
            	importarExcel1(con);
                break;                
            case "calculoCotizaSemana":
            	calculoCotizaSemana(con);
                break;                
            case "calculoCotizaMes":
            	calculoCotizaMes(con);
                break;    
            case "calculoMediasMoviles":
            	calculoMediasMoviles(con);
                break;                

            default:
                System.out.println("Acción no implementada: " + accion);
        }
    }

    private static void verClientes(Connection con) throws SQLException {
        ResultSet rs = con.createStatement().executeQuery("SELECT * FROM clientes");
        System.out.println("\n--- CLIENTES ---");
        while (rs.next()) {
            System.out.println(rs.getInt("id") + " - " + rs.getString("nombre"));
        }
    }

    private static void addCliente(Connection con) throws SQLException {
        sc.nextLine();
        System.out.print("Nombre: ");
        String nombre = sc.nextLine();

        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO clientes(nombre) VALUES (?)");
        ps.setString(1, nombre);
        ps.executeUpdate();
        System.out.println("Cliente añadido ✔");
    }

    private static void verProductos(Connection con) throws SQLException {
        ResultSet rs = con.createStatement().executeQuery("SELECT * FROM productos");
        System.out.println("\n--- PRODUCTOS ---");
        while (rs.next()) {
            System.out.println(rs.getInt("id") + " - " + rs.getString("nombre"));
        }
    }

    private static void addProducto(Connection con) throws SQLException {
        sc.nextLine();
        System.out.print("Nombre: ");
        String nombre = sc.nextLine();

        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO productos(nombre) VALUES (?)");
        ps.setString(1, nombre);
        ps.executeUpdate();
        System.out.println("Producto añadido ✔");
    }

/* importar excel formato 1 */
    /*
     *  Acción	BBVA (MSE)					
		Período	24/02/2015 - 24/02/2026					
		Moneda	EUR					
						
		Fecha	Abrir	Al alza	A la baja	cerrar	Volumen	Cambio %
		24/02/2015	8,93	8,95	8,80	8,86	33.116.569	-0,10%

     * 
     */
	private static void importarExcel1(Connection con) throws SQLException {
		
		ExcelToMySQL_formato1.exceltomysql(con, path, false);
		
	}
	
	private static void calculoCotizaSemana(Connection con) throws SQLException {
		
		CalculoCotizaSemana.calculoCotizaSemana(con);
		
	}
	
	private static void calculoCotizaMes(Connection con) throws SQLException {
		
		CalculoCotizaMes.calculoCotizaMes(con);
		
	}
	
	private static void calculoMediasMoviles(Connection con) throws SQLException {
		
		CalculoMediasMoviles.calculoMediasMoviles(con);
		
	}
	
}

// =========================
// CLASE MENUITEM
// =========================
class MenuItem {
    int id;
    String texto;
    String accion;

    public MenuItem(int id, String texto, String accion) {
        this.id = id;
        this.texto = texto;
        this.accion = accion;
    }
}
