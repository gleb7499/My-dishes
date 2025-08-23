package com.mydishes.mydishes.Parser;

/**
 * Интерфейс для отслеживания состояния операций парсинга.
 * <p>
 * Предоставляет методы обратного вызова для уведомления о начале и завершении
 * асинхронной операции парсинга. Этот интерфейс особенно полезен в случаях,
 * когда необходимо передать логику обратного вызова через несколько модулей
 * или слоев приложения, например, из Activity в Adapter, а затем в Parser,
 * как это реализовано в цепочке AddActivity -> ProductFindListAdapter -> Parser.
 * </p>
 */
public interface ParsingStateListener {
    /**
     * Вызывается непосредственно перед началом асинхронной операции парсинга.
     */
    void onParsingStarted();

    /**
     * Вызывается после завершения асинхронной операции парсинга, независимо от того,
     * была ли операция успешной или произошла ошибка.
     * <p>
     * Этот метод вызывается после {@link ProductParseCallback#onSuccess(Object)} или
     * {@link ProductParseCallback#onError(Exception)}.
     * </p>
     */
    void onParsingFinished();
}
