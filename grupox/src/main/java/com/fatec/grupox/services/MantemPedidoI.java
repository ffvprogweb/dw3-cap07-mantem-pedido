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

import com.fatec.grupox.model.ItemDePedido;
import com.fatec.grupox.model.ItemDePedidoRepository;
import com.fatec.grupox.model.Pedido;
import com.fatec.grupox.model.PedidoDTO;
import com.fatec.grupox.model.PedidoRepository;
import com.fatec.grupox.model.Produto;
import com.fatec.grupox.model.ProdutoRepository;

@Service
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
	public Pedido cadastrarPedido(PedidoDTO pedidoDTO) {
		logger.info(">>>>>> 2. servico cadastrar pedido iniciado ");
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
			logger.info(">>>>>> servico cadastrar pedido - erro nao esperado contate o administrador ");
			logger.info(">>>>>> servico cadastrar pedido - erro nao esperado => " + e.getMessage());
			logger.info(">>>>>> servico cadastrar pedido - erro nao esperado => " + e.getStackTrace());
			return null;
		}
	}

	@Override
	public List<Pedido> consultaTodos() {
		return pedidoRepository.findAll();
	}

	@Override
	public void excluiPedido(Long id) {
		pedidoRepository.deleteById(id);
	}

	/**
	 * efetiva as validações na entrada do usuario retorna pedido vazio se a entrada
	 * for invalida.
	 * 
	 * @param pedidoDTO
	 * @return pedido
	 */
	public Optional<Pedido> obtemPedido(PedidoDTO pedidoDTO) {
		// *************************************************************
		// Estrutura de dados do metodo
		// *************************************************************
		Pedido pedido = new Pedido();
		ItemDePedido item;
		Produto produto = new Produto();
		boolean cpfCadastrado = false;
		boolean produtoCadastrado = false;
		// *************************************************************
		// Valida a entrada de dados
		// *************************************************************
		cpfCadastrado = consultaCliente(pedidoDTO.getCpf());
		produtoCadastrado = consultaProduto(Long.parseLong(pedidoDTO.getProdutoId()));
		
		if (cpfCadastrado && produtoCadastrado) {
			logger.info(">>>>>> servico obtem pedido - dados validos ");
			DateTime dataAtual = new DateTime();
			DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/YYYY");
			pedido.setDataEmissao(dataAtual.toString(fmt));
			pedido.setCpf(pedidoDTO.getCpf());

			Optional<Produto> umProduto = produtoRepository.findById(Long.parseLong(pedidoDTO.getProdutoId()));
			produto = umProduto.get();
		
			item = new ItemDePedido(produto, Integer.parseInt(pedidoDTO.getQuantidade()));
			pedido.getItens().addAll(Arrays.asList(item));
			return Optional.of(pedido);
		} else {
			logger.info(">>>>>> servico obtem pedido retornou optional pedido vazio");
			return Optional.empty();
		}
	}

	@Transactional
	/**
	 * efetiva o cadastro do pedido na base cabecalho e item
	 * @param pedido a ser cadastrado (sem id)
	 * @return pedido com id
	 */
	public Pedido save(Pedido pedido) {
		logger.info(">>>>>> servico save iniciado ");
		logger.info(pedido.toString());
		Pedido umPedido = pedidoRepository.save(pedido);
		logger.info(">>>>>> cabecalho do pedido salvo no repositorio ");
		for (ItemDePedido item : pedido.getItens()) {
			item.setProduto(produtoRepository.findById(item.getProduto().getProdutoId()).get());
			item.setQuantidade(item.getQuantidade());
		}
		logger.info(pedido.getItens().get(0).getProduto().toString());
		itemRepository.saveAll(pedido.getItens());
		logger.info(">>>>>> item do pedido salvo no repositorio ");
		return umPedido;
	}

	/**
	 * verifica se o cpf do cliente esta cadastrado na base
	 * 
	 * @param cpf
	 * @return true or false
	 */
	public boolean consultaCliente(String cpf) {
		return mantemCliente.consultaPorCpf(cpf).isPresent();
	}

	/**
	 * verifica se o id de produto esta cadastrado na base
	 * 
	 * @param cod - codigo do produto
	 * @return true or false
	 */
	public boolean consultaProduto(Long cod) {
		return produtoRepository.findById(cod).isPresent();
	}
}
