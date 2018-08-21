package com.exemplo.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {

    // Inicializa texturas e formas que serao usadas durante o jogo.
    private SpriteBatch batch;
    private Texture[] passaros;
    private Texture fundo;
    private Texture canoBaixo;
    private Texture canoTopo;
    private Texture gameOver;
    private BitmapFont fonte;
    private BitmapFont mensagem;

    private Circle passaroCirculo;
    private Rectangle canoTopoForma;
    private Rectangle canoBaixoForma;

    private float larguraDispositivo;
    private float alturaDispositivo;

    private int posicaoInicialVertical;

    // Variacao e uma das 3 variacoes de imagens do passaro. Só varia posicao das asas.
    private float variacao = 0;
    private float velocidadeQueda = 0;
    private float posMovimentoCanoHorizontal;
    private float espacoEntreCanos;
    private float deltaTime;
    private float alturaEntreCanos;

    // numRandomico é usado para gerar posicoes aleatorias para os obstaculos (canos).
    private Random numRandomico;
    private int estadoJogo = 0; // 0 = Não iniciado. 1 = Iniciado. 2 = Game Over.
    private int pontuacao = 0;
    private boolean marcouPonto = false;

    // Inicializa da camera do jogador e estabelece a resolucao padrao da tela.
    private OrthographicCamera camera;
    private Viewport viewport;
    private final float VIRTUAL_WIDTH = 768;
    private final float VIRTUAL_HEIGHT = 1024;

    @Override
    public void create() {
        batch = new SpriteBatch();
        passaros = new Texture[3];
        passaros[0] = new Texture("passaro1.png");
        passaros[1] = new Texture("passaro2.png");
        passaros[2] = new Texture("passaro3.png");
        fundo = new Texture("fundo.png");
        canoBaixo = new Texture("cano_baixo.png");
        canoTopo = new Texture("cano_topo.png");
        gameOver = new Texture("game_over.png");

        larguraDispositivo = VIRTUAL_WIDTH;
        alturaDispositivo = VIRTUAL_HEIGHT;

        posicaoInicialVertical = (int) (alturaDispositivo / 2);
        posMovimentoCanoHorizontal = larguraDispositivo;
        espacoEntreCanos = 300;

        numRandomico = new Random();

        // Fonte para o texto da pontuacao atual.
        fonte = new BitmapFont();
        fonte.setColor(Color.WHITE);
        fonte.getData().setScale(6);

        mensagem = new BitmapFont();
        mensagem.setColor(Color.WHITE);
        mensagem.getData().setScale(3);

        // O passaro e representado como um circulo.
        passaroCirculo = new Circle();

        canoTopoForma = new Rectangle();
        canoBaixoForma = new Rectangle();

        // Posiciona a camera do jogador no cenario.
        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
    }

    @Override
    public void render() {

        camera.update();

        // Limpar frames anteriores.
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_COLOR_BUFFER_BIT);

        // deltaTime é o tempo de transição entre os frames do jogo.
        deltaTime = Gdx.graphics.getDeltaTime();
        variacao += deltaTime * 10;

        // Quando a variacao de imagens do passaro chega ao fim (2), a variacao e resetada.
        if (variacao > 2)
            variacao = 0;

        // Inicializa o jogo quando o jogador toca na tela.
        if (estadoJogo == 0) {
            pontuacao = 0;
            if (Gdx.input.justTouched())
                estadoJogo = 1;
        } else {

            // A velocidade de queda e constantemente incrementada para simular a gravidade.
            velocidadeQueda++;

            if (posicaoInicialVertical > 0 || velocidadeQueda < 0)
                posicaoInicialVertical -= velocidadeQueda;

            if (estadoJogo == 1) {

                // velocidadeQueda em negativo joga o passaro para cima a cada toque na tela.
                if (Gdx.input.justTouched()) {
                    velocidadeQueda = -20;
                }

                // Desloca a posicao do cano na tela para a esquerda conforme o tempo de jogo avanca.
                posMovimentoCanoHorizontal -= deltaTime * 300;

                // Se o cano ainda nao saiu da tela, nao marcou ponto.
                if (posMovimentoCanoHorizontal < -canoTopo.getWidth()) {
                    posMovimentoCanoHorizontal = larguraDispositivo;
                    alturaEntreCanos = numRandomico.nextInt(400) - 200;
                    marcouPonto = false;
                }

                // Se o cano saiu da tela, entao o passaro passou por ele e, portanto, marcou ponto.
                if (posMovimentoCanoHorizontal < 120) {
                    if (!marcouPonto) {
                        marcouPonto = true;
                        pontuacao++;
                    }
                }
                // Se o estado do jogo e Game Over, entao zera a pontuacao e reseta as posicoes dos objetos.
            } else {
                if (Gdx.input.justTouched()) {
                    estadoJogo = 0;
                    pontuacao = 0;
                    velocidadeQueda = 0;
                    posicaoInicialVertical = (int) (alturaDispositivo / 2);
                    posMovimentoCanoHorizontal = larguraDispositivo;
                    marcouPonto = false;
                }
            }
        }

        // Seta a zona de projecao da camera.
        batch.setProjectionMatrix(camera.combined);

        // Inicia a construção do batch de imagens a serem renderizadas.
        batch.begin();

        // Desenha fundo.
        batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
        // Desenha cano superior.
        batch.draw(canoTopo, posMovimentoCanoHorizontal, alturaDispositivo / 2
                + espacoEntreCanos / 2 + alturaEntreCanos);
        // Desenha cano inferior.
        batch.draw(canoBaixo, posMovimentoCanoHorizontal, alturaDispositivo / 2
                - canoBaixo.getHeight() - espacoEntreCanos / 2 + alturaEntreCanos);
        // Desenha passaro.
        batch.draw(passaros[(int) variacao], 120, posicaoInicialVertical);
        // Desenha pontuacao.
        fonte.draw(batch, String.valueOf(pontuacao), larguraDispositivo / 2, alturaDispositivo - 50);

        // Desenha tela de game over caso o jogo acabe.
        if (estadoJogo == 2) {
            batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth() / 2,
                    alturaDispositivo / 2 + gameOver.getHeight() / 2);

            mensagem.draw(batch, "Toque para reiniciar!",larguraDispositivo / 2 - 200,
                    alturaDispositivo / 2 - 50);
        }

        // Finaliza o batch de imagens a serem renderizadas.
        batch.end();

        // Posiciona os hitbox dos objetos.
        passaroCirculo.set(120 + passaros[0].getWidth() / 2,
                posicaoInicialVertical + passaros[0].getHeight() / 2,
                passaros[0].getHeight() / 2);
        canoBaixoForma.set(posMovimentoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight()
                - espacoEntreCanos / 2 + alturaEntreCanos, canoBaixo.getWidth(), canoBaixo.getHeight());
        canoTopoForma.set(posMovimentoCanoHorizontal, alturaDispositivo / 2
                + espacoEntreCanos / 2 + alturaEntreCanos, canoTopo.getWidth(), canoTopo.getHeight());

        // Verifica se o passaro bateu em algum cano, no solo ou no teto do cenario.
        if (Intersector.overlaps(passaroCirculo, canoBaixoForma) || Intersector.overlaps(passaroCirculo, canoTopoForma)
                || posicaoInicialVertical <= 0 || posicaoInicialVertical >= alturaDispositivo) {
            estadoJogo = 2;
        }
    }

    // Atualiza a proporcao/tamanho da tela de exibicao do jogo de acordo com o tamanho da tela do celular.
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    // Descarta o batch de imagens ao fim da execucao do app, para liberar memoria.
    @Override
    public void dispose() {
        batch.dispose();
        //passaros.dispose();
    }
}
