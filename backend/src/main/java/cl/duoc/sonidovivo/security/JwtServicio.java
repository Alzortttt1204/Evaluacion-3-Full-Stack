package cl.duoc.sonidovivo.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Crea y valida los tokens JWT.
 * Un JWT tiene 3 partes separadas por puntos: encabezado.datos.firma
 * La firma se calcula con la clave secreta, asi nadie puede modificar el token sin que se note.
 */
@Service
public class JwtServicio {

    private final SecretKey clave;
    private final long expiracionMinutos;

    public JwtServicio(@Value("${jwt.secreto}") String secreto,
                       @Value("${jwt.expiracion-minutos}") long expiracionMinutos) {
        if (secreto == null || secreto.isBlank()) {
            // Sin JWT_SECRET: clave aleatoria (los tokens dejan de servir al reiniciar la aplicacion)
            this.clave = Jwts.SIG.HS256.key().build();
        } else {
            // HS256 exige una clave de al menos 32 caracteres
            this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        }
        this.expiracionMinutos = expiracionMinutos;
    }

    // Genera un token con el correo del usuario como "subject" y una fecha de expiracion
    public String generarToken(String correo) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expiracionMinutos * 60_000);
        return Jwts.builder()
                .subject(correo)
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(clave)
                .compact();
    }

    // Devuelve el correo si el token es valido (firma correcta y no vencido), o null si no lo es
    public String obtenerCorreo(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(clave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public long getExpiracionMinutos() {
        return expiracionMinutos;
    }
}
