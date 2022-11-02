package com.fatec.grupox.services;
import java.util.List;
import com.fatec.grupox.model.Pedido;
import com.fatec.grupox.model.PedidoDTO;
public interface MantemPedido {
	public List<Pedido> consultaTodos();
	public void exclui(Long pedidoId);
	public Pedido cadastrar(PedidoDTO pedidoDTO);
}
