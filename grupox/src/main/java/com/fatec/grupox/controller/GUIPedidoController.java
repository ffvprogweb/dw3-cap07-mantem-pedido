package com.fatec.grupox.controller;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import com.fatec.grupox.model.ItemDePedido;
import com.fatec.grupox.model.Pedido;
import com.fatec.grupox.model.PedidoDTO;
import com.fatec.grupox.model.Produto;
import com.fatec.grupox.services.MantemPedido;

@Controller
@RequestMapping(path = "/sig")
public class GUIPedidoController {
	Logger logger = LogManager.getLogger(this.getClass());
	@Autowired
	MantemPedido mantemPedido;

	@GetMapping("/pedidos")
	public ModelAndView cadastrarPedido(PedidoDTO umPedido) {
		logger.info(">>>>>> 1. controller pagina cadastrar pedido chamada  ");
		ModelAndView mv = new ModelAndView("cadastrarPedido");
		mv.addObject("umPedido", umPedido);
		return mv;
	}

	@GetMapping("/pedido")
	public ModelAndView consultaPedidos() {
		ModelAndView mv = new ModelAndView("consultarPedido");
		logger.info(">>>>>> 1. controller chamou a api nativa de consulta de pedidos");
		mv.addObject("pedidos", mantemPedido.consultaTodos());
		return mv;
	}

	/**
	 * 
	 * @param umPedido
	 * @param result
	 * @return
	 */

	@PostMapping("/pedidos")
	public ModelAndView save(PedidoDTO pedidoDTO) {
		ModelAndView mv = new ModelAndView("consultarPedido");
		logger.info(">>>>>> 1. controller save iniciado  ");

		if (mantemPedido.cadastrarPedido(pedidoDTO) != null) {
			logger.info(">>>>>> controller save dados validos  ");
			mv.addObject("pedidos", mantemPedido.consultaTodos());
		} else {
			logger.info(">>>>>> controller save dados invalidos  ");
			mv.setViewName("cadastrarPedido");
			mv.addObject("umPedido", pedidoDTO);
			mv.addObject("message", "Dados invalidos");
		}
		return mv;
	}

	@GetMapping("/pedidos/id/{id}")
	public ModelAndView excluirPedido(@PathVariable("id") Long id) {
		logger.info(">>>>>> 1. servico de exclusao chamado ");
		mantemPedido.excluiPedido(id);
		ModelAndView modelAndView = new ModelAndView("consultarPedido");
		modelAndView.addObject("pedidos", mantemPedido.consultaTodos());
		return modelAndView;
	}
}
