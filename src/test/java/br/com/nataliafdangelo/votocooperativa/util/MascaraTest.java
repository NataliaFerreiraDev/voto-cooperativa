package br.com.nataliafdangelo.votocooperativa.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MascaraTest {

    @Test
    void deveMascararCpfMantendoApenasOsDoisUltimosDigitos() {
        assertThat(Mascara.cpf("12345678900")).isEqualTo("*********00");
    }

    @Test
    void deveRetornarAsteriscosQuandoValorForNulo() {
        assertThat(Mascara.cpf(null)).isEqualTo("***");
    }

    @Test
    void deveRetornarAsteriscosQuandoValorForMuitoCurto() {
        assertThat(Mascara.cpf("12")).isEqualTo("***");
    }

}
