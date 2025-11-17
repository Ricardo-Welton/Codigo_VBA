import { ChangeDetectionStrategy, Component, signal, OnInit, computed } from '@angular/core';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';

// Interfaces
interface Beneficio {
  id: number;
  nome: string;
  valor: number;
}

interface TransferRequest {
  fromId: number;
  toId: number;
  amount: number;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule,FormsModule, HttpClientModule],
  template: `
    <div class="min-h-screen bg-gray-100 p-4 sm:p-8">
      <div class="max-w-4xl mx-auto">
        <h1 class="text-3xl font-extrabold text-indigo-700 mb-6 border-b-4 border-indigo-500 pb-2">
          Gerenciador de Benefícios (Angular + Spring EJB)
        </h1>

        <!-- Formulário de Transferência (Bug Fix Demo) -->
        <div class="bg-white p-6 rounded-lg shadow-xl mb-8">
          <h2 class="text-2xl font-semibold text-indigo-600 mb-4">Transferência Segura (EJB Lock Demo)</h2>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <input #fromId type="number" placeholder="ID Origem" class="p-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
            <input #toId type="number" placeholder="ID Destino" class="p-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
            <input #amount type="number" placeholder="Valor" class="p-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
          </div>
          <button (click)="submitTransfer(fromId.value, toId.value, amount.value)"
                  class="mt-4 w-full bg-green-500 hover:bg-green-600 text-white font-bold py-2 px-4 rounded-lg transition duration-150">
            {{ isTransferring() ? 'Transferindo...' : 'Transferir Valor' }}
          </button>
          @if (transferMessage()) {
            <p class="mt-3 p-3 rounded-lg text-sm" 
               [ngClass]="{'bg-green-100 text-green-700': !transferError(), 'bg-red-100 text-red-700': transferError()}">
              {{ transferMessage() }}
            </p>
          }
        </div>

        <!-- Formulário para Criar e Editar -->
        <div class="bg-white p-6 rounded-lg shadow-xl mb-8">
          <h2 class="text-2xl font-semibold text-indigo-600 mb-4">{{ isEditing() ? 'Editar Benefício' : 'Adicionar Novo Benefício' }}</h2>
          <div class="flex flex-col sm:flex-row gap-4">
            <input [(ngModel)]="currentBeneficio.nome" type="text" placeholder="Nome do Benefício" required 
                   class="flex-grow p-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
            <input [(ngModel)]="currentBeneficio.valor" type="number" placeholder="Valor Inicial" required 
                   class="w-full sm:w-40 p-2 border rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
            <button (click)="saveBeneficio()"
                    [disabled]="isSaving()"
                    class="bg-indigo-500 hover:bg-indigo-600 text-white font-bold py-2 px-4 rounded-lg transition duration-150 disabled:opacity-50">
              {{ isSaving() ? 'Salvando...' : (isEditing() ? 'Atualizar' : 'Criar') }}
            </button>
          </div>
        </div>

        <!-- Lista de Benefícios -->
        <div class="bg-white p-6 rounded-lg shadow-xl">
          <h2 class="text-2xl font-semibold text-indigo-600 mb-4">Lista de Benefícios</h2>
          @if (isLoading()) {
            <p class="text-center text-gray-500">Carregando benefícios...</p>
          } @else if (beneficios().length === 0) {
            <p class="text-center text-gray-500">Nenhum benefício cadastrado.</p>
          } @else {
            <div class="space-y-3">
              @for (beneficio of sortedBeneficios(); track beneficio.id) {
                <div class="flex items-center justify-between p-4 bg-indigo-50 rounded-lg border border-indigo-200 hover:bg-indigo-100 transition duration-150">
                  <div class="flex-grow">
                    <p class="text-lg font-bold text-indigo-800">{{ beneficio.nome }}</p>
                    <p class="text-sm text-gray-600">ID: {{ beneficio.id }} | Saldo: <span class="font-mono text-green-700">R$ {{ beneficio.valor | number:'1.2-2' }}</span></p>
                  </div>
                  <div class="flex space-x-2">
                    <button (click)="edit(beneficio)" class="text-blue-600 hover:text-blue-800 p-1 rounded-full hover:bg-blue-200">
                      <!-- Edit Icon -->
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor"><path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zm-3.793 4.414L3 14.586V17h2.414l7.586-7.586-2.828-2.828z"/></svg>
                    </button>
                    <button (click)="deleteBeneficio(beneficio.id)" class="text-red-600 hover:text-red-800 p-1 rounded-full hover:bg-red-200">
                      <!-- Delete Icon -->
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm4 0a1 1 0 112 0v6a1 1 0 11-2 0V8z" clip-rule="evenodd"/></svg>
                    </button>
                  </div>
                </div>
              }
            </div>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    /* Angular Signal/Input-based CSS */
    .p-2.border:focus {
      outline: none;
      box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.4); /* Tailwind indigo-500 shadow */
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App implements OnInit {
  private apiUrl = '/api/v1/beneficios';
  
  beneficios = signal<Beneficio[]>([]);
  isLoading = signal(true);
  isSaving = signal(false);
  isTransferring = signal(false);
  
  transferMessage = signal<string | null>(null);
  transferError = signal(false);

  currentBeneficio = {
    id: null as number | null,
    nome: '',
    valor: 0,
  };
  
  isEditing = computed(() => this.currentBeneficio.id !== null);
  sortedBeneficios = computed(() => 
    [...this.beneficios()].sort((a, b) => a.id - b.id)
  );

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.fetchBeneficios();
  }

  // --- CRUD ---

  fetchBeneficios(): void {
    this.isLoading.set(true);
    this.http.get<Beneficio[]>(this.apiUrl)
      .subscribe({
        next: (data) => {
          this.beneficios.set(data);
          this.isLoading.set(false);
        },
        error: (err) => {
          console.error('Erro ao buscar benefícios:', err);
          this.isLoading.set(false);
        }
      });
  }

  saveBeneficio(): void {
    this.isSaving.set(true);
    const beneficioData = {
      nome: this.currentBeneficio.nome,
      valor: this.currentBeneficio.valor,
    };

    if (this.isEditing()) {
      // Atualização
      this.http.put<Beneficio>(`${this.apiUrl}/${this.currentBeneficio.id}`, beneficioData)
        .subscribe({
          next: () => {
            this.fetchBeneficios();
            this.resetForm();
          },
          error: (err) => console.error('Erro ao atualizar:', err),
          complete: () => this.isSaving.set(false)
        });
    } else {
      // Novo
      this.http.post<Beneficio>(this.apiUrl, beneficioData)
        .subscribe({
          next: () => {
            this.fetchBeneficios();
            this.resetForm();
          },
          error: (err) => console.error('Erro ao criar:', err),
          complete: () => this.isSaving.set(false)
        });
    }
  }

  edit(beneficio: Beneficio): void {
    this.currentBeneficio.id = beneficio.id;
    this.currentBeneficio.nome = beneficio.nome;
    this.currentBeneficio.valor = beneficio.valor;
  }

  deleteBeneficio(id: number): void {
    if (!confirm('Tem certeza que deseja deletar este benefício?')) { 
      return;
    }
    this.http.delete(`${this.apiUrl}/${id}`)
      .subscribe({
        next: () => this.fetchBeneficios(),
        error: (err) => console.error('Erro ao deletar:', err)
      });
  }

  resetForm(): void {
    this.currentBeneficio.id = null;
    this.currentBeneficio.nome = '';
    this.currentBeneficio.valor = 0;
  }
  
  // --- transferencia ---

  submitTransfer(fromId: string, toId: string, amount: string): void {
    const fromIdNum = parseInt(fromId, 10);
    const toIdNum = parseInt(toId, 10);
    const amountNum = parseFloat(amount);

    if (isNaN(fromIdNum) || isNaN(toIdNum) || isNaN(amountNum) || amountNum <= 0) {
      this.transferError.set(true);
      this.transferMessage.set('Preencha IDs válidos e um valor positivo.');
      return;
    }

    this.isTransferring.set(true);
    this.transferMessage.set(null);
    this.transferError.set(false);

    const requestBody: TransferRequest = { 
      fromId: fromIdNum, 
      toId: toIdNum, 
      amount: amountNum 
    };

    this.http.post<any>(`${this.apiUrl}/transfer`, requestBody)
      .subscribe({
        next: (res) => {
          this.transferError.set(false);
          this.transferMessage.set('Sucesso: ' + (res.message || 'Transferência concluída.'));
          this.fetchBeneficios(); // Atualizar a lista
        },
        error: (err) => {
          this.transferError.set(true);
          this.transferMessage.set('Falha: ' + (err.error?.error || 'Erro desconhecido na transferência.'));
          console.error('Transfer Error:', err);
        },
        complete: () => this.isTransferring.set(false)
      });
  }
}