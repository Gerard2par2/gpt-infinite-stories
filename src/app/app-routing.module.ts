import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { WorldsComponent } from "./worlds/worlds.component";
import { GamesComponent } from "./games/games.component";

const routes: Routes = [
  { path: '', redirectTo: '/worlds', pathMatch: 'full' },
  { path: 'worlds', component: WorldsComponent },
  { path: 'games', component: GamesComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
