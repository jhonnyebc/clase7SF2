import { describe, expect, it } from 'vitest';
import { iniciales, leerNota, normalizar, notaUnDecimal } from './formato';

describe('leerNota', () => {
  it('acepta de 0 a 5 con un decimal, con coma o punto', () => {
    expect(leerNota('0')).toBe(0);
    expect(leerNota('5')).toBe(5);
    expect(leerNota('4,2')).toBe(4.2);
    expect(leerNota(' 3.5 ')).toBe(3.5);
  });

  it('rechaza valores fuera de rango, con dos decimales o que no son numeros', () => {
    for (const texto of ['5.1', '-1', '3.25', '10', '', 'abc', '4,']) {
      expect(leerNota(texto)).toBeNull();
    }
  });
});

describe('ayudas de formato', () => {
  it('muestra la nota con coma decimal', () => expect(notaUnDecimal(4.2)).toBe('4,2'));
  it('saca las iniciales en mayuscula', () => expect(iniciales('juan', 'pérez')).toBe('JP'));
  it('busca ignorando tildes y mayusculas', () => expect(normalizar('Sofía LÓPEZ')).toBe('sofia lopez'));
});
