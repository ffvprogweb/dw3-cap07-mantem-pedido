package com.fatec.grupox.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fatec.grupox.model.Cliente;
import com.fatec.grupox.model.ItemDePedido;
import com.fatec.grupox.model.ItemDePedidoRepository;
import com.fatec.grupox.model.Pedido;
import com.fatec.grupox.model.PedidoDTO;
import com.fatec.grupox.model.PedidoRepository;
import com.fatec.grupox.model.Produto;
import com.fatec.grupox.model.ProdutoRepository;

@Service
/**
 * Implementa as regras de negocio para cadastrar, consultar e excluir pedido 
 * @author programador1
 *
 */
public class MantemPedidoI implements MantemPedido {
	Logger logger = LogManager.getLogger(this.getClass());
	@Autowired
	private PedidoRepository pedidoRepository;
	@Autowired
	private ProdutoRepository produtoRepository;
	@Autowired
	private ItemDePedidoRepository itemRepository;
	@Autowired
	private MantemCliente mantemCliente;

	/**
	 * se a entrada de dados para o objeto pedido for valida chama o metodo save
	 * 
	 * @param pedidoDTO
	 * @return pedido ou null
	 */
	@Transactional
	@Override
	public Pedido cadastrar(PedidoDTO pedidoDTO) {
		logger.info(">>>>>> servico cadastrar pedido iniciado ");
		try {
			Optional<Pedido> umPedido = obtemPedido(pedidoDTO);
			if (umPedido.isPresent()) {
				logger.info(">>>>>> servico cadastrar pedido - dados validos ");
				return save(umPedido.get());
			} else {
				logger.info(">>>>>> servico cadastrar pedido - dados invalidos ");
				return null;
			}
		} catch (Exception e) {
			logger.info(">>>>>> servico cadastrar pedido - erro nao esperado => " + e.getMessage());
			return null;
		}
	}

	@Override
	public List<Pedido> consultaTodos() {
		return pedidoRepository.findAll();
	}

	@Override
	public void exclui(Long id) {
		pedidoRepository.deleteById(id);
	}

	/**
	 * converte pedidodto para pedido
	 * verifica a entrada de usuario se o cliente existe e se o produto existe  
	 * 
	 * @param entra pedidoDTO
	 * @return pedido vazio se a entrada for invalida
	 */
	public Optional<Pedido> obtemPedido(PedidoDTO pedidoDTO) {
		// *************************************************************
		// Estrutura de dados do metodo
		// *************************************************************
		Pedido pedido;
		ItemDePedido item;
		Optional<Produto> produto;
		Optional<Cliente> cliente;
		// *************************************************************
		// Valida a entrada de dados (nao valida quantidade = 0)
		// *************************************************************
		cliente = consultaCliente(pedidoDTO.getCpf());
		produto = consultaPedido(Long.parseLong(pedidoDTO.getProdutoId()));
		
		if (cliente.isPresent() && produto.isPresent()) {
			pedido = new Pedido();
			logger.info(">>>>>> servico obtem pedido - dados validos ");
			DateTime dataAtual = new DateTime();
			DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/YYYY");
			pedido.setDataEmissao(dataAtual.toString(fmt));
			pedido.setCpf(pedidoDTO.getCpf());
			item = new ItemDePedido(produto.get(), Integer.parseInt(pedidoDTO.getQuantidade()));
			pedido.getItens().addAll(Arrays.asList(item));
			return Optional.of(pedido);
		} else {
			logger.info(">>>>>> servico obtem pedido retornou optional pedido vazio");
			return Optional.empty();
		}
	}

	@Transactional
	/**
	 * efetiva o cadastro do pedido no banco de dados primeiro cabecalho depois item
	 * 
	 * @param pedido a ser cadastrado (sem id)
	 * @return pedido com id
	 */
	public Pedido save(Pedido pedido) {
		logger.info(">>>>>> servico save iniciado ");
		Pedido umPedido = pedidoRepository.save(pedido);
		logger.info(">>>>>> servico save cabecalho do pedido salvo no repositorio ");
		for (ItemDePedido item : pedido.getItens()) {
			//item.setProduto(produtoRepository.findById(item.getProduto().getProdutoId()).get());
			item.setProduto(item.getProduto());
			item.setQuantidade(item.getQuantidade());
		}
		itemRepository.saveAll(pedido.getItens());
		logger.info(">>>>>> servico save item do pedido salvo no repositorio ");
		return umPedido;
	}

	/**
	 * verifica se o cliente esta cadastrado no banco de dados
	 * 
	 * @param cpf
	 * @return true or false
	 */
	public Optional<Cliente> consultaCliente(String cpf) {
		return mantemCliente.consultaPorCpf(cpf);
	}

	/**
	 * verifica se o produto esta cadastrado no banco de dados
	 * 
	 * @param id - codigo do produto
	 * @return optional de produto
	 */
	public Optional<Produto> consultaPedido(Long id) {
		return produtoRepository.findById(id);
	}
}
