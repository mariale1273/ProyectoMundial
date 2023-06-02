package proyectomundial.DAO;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import proyectomundial.model.*;
import proyectomundial.util.BasedeDatos;

/**
 *
 * @author Hiro & Camily
 */
public class ResultadoDAO {
    public static int totalInt;

    public double promedioGoles;
    // cantPartidosGanadorEmpate [0] cantidad partidos con ganador
    // cantPartidosGanadorEmpate [1] cantidad partidos empatados
    public int[] cantPartidosGanadorEmpate;
    // seleccionPuntos [0] seleccion con mayor cantidad de puntos
    // seleccionPuntos [1] seleccion con menor cantidad de puntos
    public String[] seleccionPuntos;

    public ResultadoDAO() {
        BasedeDatos.conectar();

    }

    public boolean registrarResultado(Resultado resultado) {

        String sql = "INSERT INTO m_leal7.partido (id, grupo, local, visitante, continente_local, continente_visitante, goles_local, goles_visitante) values("
                + "'" + resultado.getId() + "', "
                + "'" + resultado.getGrupo() + "', "
                + "'" + resultado.getLocal() + "', "
                + "'" + resultado.getVisitante() + "', "
                + "'" + resultado.getContinenteLocal() + "', "
                + "'" + resultado.getContinenteVisitante() + "', "
                + "'" + resultado.getGolesLocal() + "', "
                + "'" + resultado.getGolesVisitantes() + "')";

        boolean registro = BasedeDatos.ejecutarActualizacionSQL(sql);

        return registro;
    }

    public List<Resultado> getResultado() {

        String sql = "SELECT distinct * FROM m_leal7.partido ORDER BY id ASC";
        List<Resultado> resultados = new ArrayList<Resultado>();
        totalInt = 0;
        try {
            ResultSet result = BasedeDatos.ejecutarSQL(sql);

            if (result != null) {

                while (result.next()) {
                    Resultado resultado = new Resultado(result.getInt("id"), result.getString("grupo").charAt(0),
                            result.getString("local"), result.getString("visitante"),
                            result.getString("continente_local"), result.getString("continente_visitante"),
                            result.getInt("goles_local"), result.getInt("goles_visitante"));
                    resultados.add(resultado);
                    totalInt++;
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            System.out.println("Error consultando resultados");
        }

        return resultados;
    }

    public List<Resultado> getResultado(String dato) {

        String sql = "SELECT distinct * FROM m_leal7.partido where "
                + "grupo iLIKE '%" + dato + "%' "
                + "OR local iLIKE '%" + dato + "%' "
                + "OR visitante iLIKE '%" + dato + "%' "
                + "OR continente_local iLIKE '%" + dato + "%' "
                + "OR continente_visitante iLIKE '%" + dato + "%' "
                + "OR CAST(goles_local AS VARCHAR(30)) iLIKE '%" + dato + "%' "
                + "OR CAST(goles_visitante AS VARCHAR(30)) iLIKE '%" + dato + "%' "
                + "ORDER BY id ASC";

        List<Resultado> resultados = new ArrayList<Resultado>();

        try {
            ResultSet result = BasedeDatos.ejecutarSQL(sql);

            if (result != null) {

                while (result.next()) {
                    Resultado resultado = new Resultado(result.getInt("id"), result.getString("grupo").charAt(0),
                            result.getString("local"), result.getString("visitante"),
                            result.getString("continente_local"), result.getString("continente_visitante"),
                            result.getInt("goles_local"), result.getInt("goles_visitante"));
                    resultados.add(resultado);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            System.out.println("Error consultando resultados");
        }

        return resultados;
    }

    public String[][] getResultadoMatriz() {

        String[][] matrizResultados = null;
        List<Resultado> resultados = getResultado();

        setInfoDashboard(resultados);

        if (resultados != null && resultados.size() > 0) {

            matrizResultados = new String[resultados.size()][8];

            int x = 0;
            for (Resultado resultado : resultados) {

                matrizResultados[x][0] = String.valueOf(resultado.getId());
                matrizResultados[x][1] = String.valueOf(resultado.getGrupo());
                matrizResultados[x][2] = resultado.getLocal();
                matrizResultados[x][3] = resultado.getVisitante();
                matrizResultados[x][4] = resultado.getContinenteLocal();
                matrizResultados[x][5] = resultado.getContinenteVisitante();
                matrizResultados[x][6] = String.valueOf(resultado.getGolesLocal());
                matrizResultados[x][7] = String.valueOf(resultado.getGolesVisitantes());
                x++;
            }
        }

        return matrizResultados;
    }

    public String[][] getResultadoMatriz(String dato) {

        String[][] matrizResultados = null;
        List<Resultado> resultados = getResultado(dato);

        if (resultados != null && resultados.size() > 0) {

            matrizResultados = new String[resultados.size()][8];

            int x = 0;
            for (Resultado resultado : resultados) {

                matrizResultados[x][0] = String.valueOf(resultado.getId());
                matrizResultados[x][1] = String.valueOf(resultado.getGrupo());
                matrizResultados[x][2] = resultado.getLocal();
                matrizResultados[x][3] = resultado.getVisitante();
                matrizResultados[x][4] = resultado.getContinenteLocal();
                matrizResultados[x][5] = resultado.getContinenteVisitante();
                matrizResultados[x][6] = String.valueOf(resultado.getGolesLocal());
                matrizResultados[x][7] = String.valueOf(resultado.getGolesVisitantes());
                x++;
            }
        }

        return matrizResultados;
    }

    public void setInfoDashboard(List<Resultado> resultados) {
        if (resultados != null && resultados.size() > 0) {
            // Limpio variables globales
            promedioGoles = 0;
            cantPartidosGanadorEmpate = new int[2];
            seleccionPuntos = new String[2];
            // Variables locales necesarias
            int cantGoles = 0;
            List<Seleccion> selecciones = new SeleccionDAO().getSelecciones();

            for (Resultado resultado : resultados) {
                cantGoles += resultado.getGolesLocal() + resultado.getGolesVisitantes();
                if (resultado.getGolesLocal() == resultado.getGolesVisitantes()) {
                    cantPartidosGanadorEmpate[1]++;
                } else {
                    cantPartidosGanadorEmpate[0]++;
                }
                selecciones = getSeleccionesActGoles(selecciones, resultado);
            }
            int cantPuntosMayor = 0;
            int cantPuntosMenor = 0;
            for (Seleccion seleccion : selecciones) {
                if (seleccion.getGol() > cantPuntosMayor) {
                    cantPuntosMayor = seleccion.getGol();
                    seleccionPuntos[0] = seleccion.getNombre();
                } else if (seleccion.getGol() < cantPuntosMenor) {
                    cantPuntosMenor = seleccion.getGol();
                    seleccionPuntos[1] = seleccion.getNombre();
                }
            }
            promedioGoles = cantGoles / resultados.size();
        }
    }

    public List<Seleccion> getSeleccionesActGoles(List<Seleccion> selecciones, Resultado resultado) {
        for (int i = 0; i < selecciones.size(); i++) {
            if (selecciones.get(i).getNombre().equalsIgnoreCase(resultado.getLocal())) {
                selecciones.get(i).setGol(selecciones.get(i).getGol() + resultado.getGolesLocal());
            } else if (selecciones.get(i).getNombre().equalsIgnoreCase(resultado.getVisitante())) {
                selecciones.get(i).setGol(selecciones.get(i).getGol() + resultado.getGolesVisitantes());
            }
        }
        return selecciones;
    }
}