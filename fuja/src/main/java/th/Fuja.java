package th;

import gearth.extensions.Extension;
import gearth.extensions.ExtensionInfo;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ExtensionInfo(Title = "Fuja", Description = "Fuja", Version = "1.1", Author = "thiago")
public class Fuja extends Extension {

	 boolean jaEnviou = false;
	 boolean precisaEnviar = false;
	 Integer ultimoQuarto = null;
	 Integer posicaoOrigemX = null;
	 Integer posicaoOrigemY = null;
	 Integer posicaoFinalX = null;
	 Integer posicaoFinalY = null;
	 boolean aguardandoCliqueInicial = false;
	 boolean aguardandoCliqueFinal = false;
	 boolean podeFalarPosInicial = false;
	 boolean podeFalarPosFinal = false;
	 private volatile boolean andandoAutomatico = false;
	 int fase = 0;
	

	public static void main(String[] args) {
		new Fuja(args).run();
	}

	public Fuja(String[] args) {
		super(args);
	}

	@Override
	protected void initExtension() {
		trocouDeQuarto();
		capturarQuantidadeDeEsferas();
		capturaMovimentoDasEsferas();
		setarPosicaoDeComeço();
		capturarPosicaoInicialDoPersonagem();
		setarPosicaoDeDestino();
		capturarPosicaoFinalDoPersonagem();
	}

	private void andarAtePosicaoDeDestino() {
		sendToServer(new HPacket("MoveAvatar", HMessage.Direction.TOSERVER, posicaoFinalX, posicaoFinalY));
	}

	private void setarPosicaoDeComeço() {
	intercept(HMessage.Direction.TOSERVER, "Chat", hMessage -> {
		HPacket packet = hMessage.getPacket();
		String setar = packet.readString();
		 if(setar.equalsIgnoreCase(":s")) {
	            hMessage.setBlocked(true);
	            aguardandoCliqueInicial = true;
	            sendToClient(new HPacket("Chat", HMessage.Direction.TOCLIENT, -1, "Clique no Local de Ínicio", 0, 1, 0, -1));
	            podeFalarPosInicial = true;
	        }  
	});	
	}

	private void iniciarMovimentoAutomatico(int intervaloMs) {
	    if (andandoAutomatico) return; 

	    andandoAutomatico = true;

	    new Thread(() -> {
	        while (andandoAutomatico) {
	            try {
	                andarAtePosicaoDeDestino();
	                Thread.sleep(intervaloMs); 
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
	    }).start();
	}

	private void pararMovimentoAutomatico() {
	    andandoAutomatico = false;
	}

	
	private void setarPosicaoDeDestino() {
		intercept(HMessage.Direction.TOSERVER, "Chat", hMessage -> {
			HPacket packet = hMessage.getPacket();
			String setar = packet.readString();
			 if(setar.equalsIgnoreCase(":f")) {
		            hMessage.setBlocked(true);
		            aguardandoCliqueFinal = true;
		            sendToClient(new HPacket("Chat", HMessage.Direction.TOCLIENT, -1, "Clique no Local de destino", 0, 1, 0, -1));
		            podeFalarPosFinal = true;
		        }  
		});	
		}
	
	private void digitarPraParar() {
		intercept(HMessage.Direction.TOSERVER, "Chat", hMessage -> {
			HPacket packet = hMessage.getPacket();
			String setar = packet.readString();
			 if(setar.equalsIgnoreCase(":p")) {
		            hMessage.setBlocked(true);
		            posicaoFinalX = null;
		            posicaoFinalY = null;
		        }  
		});	
		}

	private void capturaMovimentoDasEsferas() {
		intercept(HMessage.Direction.TOCLIENT, "WiredMovements", hMessage -> {
			HPacket packet = hMessage.getPacket();
			try {
				int qtd = packet.readInteger(); 
				for(int i = 0; i < qtd ;i++) {
				 packet.readInteger(); 
				 int origemPosicaoEsferaX = packet.readInteger();
				 int origemPosicaoEsferaY = packet.readInteger();
				 int destinoPosicaoEsferaX = packet.readInteger();
				 int destinoPosicaoEsferaY = packet.readInteger();
				 
				 packet.readString();
				 packet.readString();
				 
				 int iddaBola = packet.readInteger();
				 packet.readInteger();
				 packet.readInteger();
				 
//				 System.out.println("=========ORIGEM===========");
//				 System.out.println(origemPosicaoEsferaX);
//				 System.out.println(origemPosicaoEsferaY);
//				 System.out.println("=========DESTINO===========");
//				 System.out.println(destinoPosicaoEsferaX);
//				 System.out.println(destinoPosicaoEsferaY);
//				 System.out.println("=========ID DA BOLA===========");
//				 System.out.println(iddaBola); 
				}
			} catch (Exception e) {
				
			}
		});
	}

	private void capturarPosicaoInicialDoPersonagem() {
	    intercept(HMessage.Direction.TOSERVER, "MoveAvatar", hMessage -> {
	        HPacket packet = hMessage.getPacket();
	        int x = packet.readInteger();
	        int y = packet.readInteger();
	        if(aguardandoCliqueInicial) {
	        	hMessage.setBlocked(true);
	        	posicaoOrigemX = x;
	        	posicaoOrigemY = y;
	        }
	        aguardandoCliqueInicial = false;   
	        System.out.println("posicao origem x: " + posicaoOrigemX + " posicao origem y: " + posicaoOrigemY);
	        if(podeFalarPosInicial) {
	        	sendToClient(new HPacket("Chat", HMessage.Direction.TOCLIENT, -1, "Pos inicial capturada. " + posicaoOrigemX + "  " + posicaoOrigemY, 0, 1, 0, -1));
	        }
	        podeFalarPosInicial = false;
	    });
	    
	}

	private void capturarPosicaoFinalDoPersonagem() {
		 intercept(HMessage.Direction.TOSERVER, "MoveAvatar", hMessage -> {
		        HPacket packet = hMessage.getPacket();
		        int h = packet.readInteger();
		        int z = packet.readInteger();
		        if(aguardandoCliqueFinal) {
		        	hMessage.setBlocked(true);
		        	posicaoFinalX = h;
		        	posicaoFinalY = z;
		        }
		        aguardandoCliqueFinal = false;   
		        System.out.println("posicao destino x: " + posicaoFinalX + " posicao destino y: " + posicaoFinalY);
		        if(podeFalarPosFinal) {
		        	sendToClient(new HPacket("Chat", HMessage.Direction.TOCLIENT, -1, "Pos Final capturada. " + posicaoFinalX + "  " + posicaoFinalY, 0, 1, 0, -1));
		        }
		        podeFalarPosFinal = false;
		        iniciarMovimentoAutomatico(1000);
		 });
	}

	private List<Integer> capturarQuantidadeDeEsferas() {
		List<Integer> esferas = new ArrayList<>();
		intercept(HMessage.Direction.TOCLIENT, "WiredMovements", hMessage -> {
			HPacket packet = hMessage.getPacket();
			try {
				int qtdEsferas = packet.readInteger();
				for (int i = 0; i < qtdEsferas; i++) {
					packet.readInteger();
					packet.readInteger();
					packet.readInteger();
					packet.readInteger();
					packet.readInteger();

					packet.readString();
					packet.readString();

					int esfera = packet.readInteger();
					if (esfera > 999999) {
						esferas.add(esfera);
					}
					packet.readInteger();
					packet.readInteger();
				}
				enviarMsg(esferas);
				precisaEnviar = false;
			} catch (Exception e) {
			}
		});
		return esferas;
	}

	private void trocouDeQuarto() {
		try {
			intercept(HMessage.Direction.TOSERVER, "GetGuestRoom", hMessage -> {
			    HPacket packet = hMessage.getPacket();

			    int quartoAtual = packet.readInteger();
			    packet.readInteger();
			    packet.readInteger();

			    if (ultimoQuarto == null || quartoAtual != ultimoQuarto) {
			        System.out.println("Mudou de quarto: " + quartoAtual);
			        ultimoQuarto = quartoAtual;

			        precisaEnviar = true;
			        jaEnviou = false;
			    }
			});	
		} catch (Exception e) {			
		}			
	}
	protected String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 6) { 
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
	private void enviarMsg(List<Integer> esferas) {
		if (jaEnviou)
			return;
		jaEnviou = true;
		sendToServer(new HPacket("Chat", HMessage.Direction.TOSERVER, "Quantidade de esferas : " + esferas.size() + "   -    " + getSaltString(), 0, -1));
	}
}