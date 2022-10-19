package com.fatec.grupox;

import java.util.Arrays;
import java.util.Optional;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fatec.grupox.model.Cliente;
import com.fatec.grupox.model.ItemDePedido;
import com.fatec.grupox.model.Pedido;
import com.fatec.grupox.model.Produto;
import com.fatec.grupox.model.ProdutoRepository;
import com.fatec.grupox.model.UserAppRepository;
import com.fatec.grupox.model.UsuarioApp;
import com.fatec.grupox.services.MantemCliente;
import com.fatec.grupox.services.MantemPedidoI;

@SpringBootApplication
public class GrupoxApplication {
	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	UserAppRepository repository;
	@Autowired
	private MantemCliente mantemCliente;
	@Autowired
	private ProdutoRepository produtoRepository;
	@Autowired
	private MantemPedidoI pedidoServico;


	public static void main(String[] args) {
		SpringApplication.run(GrupoxApplication.class, args);
		System.out.println(new BCryptPasswordEncoder().encode("123"));
	}

	@Autowired
	public void inicializa() {
		UsuarioApp usuario = new UsuarioApp();
		usuario.setUserName("jose");
		usuario.setPassword(passwordEncoder.encode("123"));
		repository.save(usuario);
		// ******************************************************************************************
		// Cadastrar tres clientes na base - a cliente Claudia não comprou nada
		// ******************************************************************************************
		Cliente umCliente = new Cliente("Andrade", "25/05/1960", "M", "99504993052", "04280130", "1234");
		umCliente.setProfissao("Advogado");
		mantemCliente.save(umCliente);
		umCliente = new Cliente("Silva", "18/03/1964", "M", "43011831084", "08545160", "1234");
		umCliente.setProfissao("Técnico");
		mantemCliente.save(umCliente);

		umCliente = new Cliente("Claudia", "11/05/1974", "F", "85765535380", "08545160", "1234");
		umCliente.setProfissao("Técnico");
		mantemCliente.save(umCliente);
		// ******************************************************************************************
		// Cadastrar tres produtos na base de dados
		// ******************************************************************************************
		Produto produto1 = new Produto(1L, "parafuso", 10, 30); // descricao, custo e quantidade no estoque
		Produto produto2 = new Produto(2L, "tijolo", 15, 60);
		Produto produto3 = new Produto(3L, "bucha", 5, 50);
		produtoRepository.saveAll(Arrays.asList(produto1, produto2, produto3));
		// *******************************************************************************************
		// Cadastrar Pedido1 - entrada de dados de pedido - o cliente deve estar
		// previamente cadastrado
		// informações sobre o nome do cliente, endereco sao obtidos com base no cpf do
		// cliente
		// *******************************************************************************************
		Pedido pedido1 = new Pedido("43011831084");
		// *******************************************************************************************
		// Detalhes do pedido - o produto deve estar cadastrado este cliente comprou 2
		// itens parafuso e bucha
		// *******************************************************************************************
		Optional<Produto> umProduto = produtoRepository.findById(1L);
		Produto produtoComprado1 = umProduto.get();
		umProduto = produtoRepository.findById(3L);
		Produto produtoComprado2 = umProduto.get();
		ItemDePedido ip1 = new ItemDePedido(produtoComprado1, 20); // quantidade comprada
		ItemDePedido ip2 = new ItemDePedido(produtoComprado2, 10); // quantidade comprada
		// *******************************************************************************************
		// adiciona os itens comprados no pedido e salva
		// *******************************************************************************************
		DateTime dataAtual = new DateTime();
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/YYYY");
		pedido1.setDataEmissao(dataAtual.toString(fmt));
		pedido1.getItens().addAll(Arrays.asList(ip1, ip2));
		pedidoServico.save(pedido1);
		// *******************************************************************************************
		// Cadastrar Pedido 2 - entrada de dados este cliente comprou somente um item
		// tijolo
		// *******************************************************************************************
		Pedido pedido2 = new Pedido("43011831084");
		umProduto = produtoRepository.findById(2L);
		produtoComprado1 = umProduto.get();
		ip1 = new ItemDePedido(produtoComprado1, 20); // quantidade comprada
		pedido2.getItens().addAll(Arrays.asList(ip1));
		pedidoServico.save(pedido2);
		// *******************************************************************************************
		// Cadastrar Pedido 3 - entrada de dados este cliente comprou somente bucha
		// *******************************************************************************************
		Pedido pedido3 = new Pedido("99504993052");
		umProduto = produtoRepository.findById(3L);
		produtoComprado1 = umProduto.get();
		ip1 = new ItemDePedido(produtoComprado1, 12); // quantidade comprada
		pedido3.getItens().addAll(Arrays.asList(ip1));
		pedidoServico.save(pedido3);

	}

	public BCryptPasswordEncoder pc() {
		return new BCryptPasswordEncoder();
	}

}
